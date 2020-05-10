package tr.edu.itu.bbf.cloudcore.distributed.service;
import com.esotericsoftware.kryo.Kryo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import tr.edu.itu.bbf.cloudcore.distributed.ipc.CkptMessage;
import tr.edu.itu.bbf.cloudcore.distributed.ipc.Response;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Service
public class StateMachineWorker {

    static class ExitHook extends Thread {
        @Autowired
        private StateMachine<States, Events> stateMachine;

        private Scanner scanner;

        static final Logger logger = LoggerFactory.getLogger(ExitHook.class);

        public ExitHook(StateMachine<States,Events> sm, Scanner sc){
            this.stateMachine = sm;
            this.scanner = sc;
        }

        @Override
        public void run(){
            logger.info("**********************************");
            logger.info("*****Gracefully stopping SMOC*****");
            logger.info("**********************************");
            this.scanner.close();
            this.stateMachine.stop();
        }
    }
    /*
    @Autowired
    private StateMachine<States, Events> stateMachine;
     */


    @Autowired
    @Qualifier("factory_without_ZK")
    private StateMachineFactory<States, Events> factory_without_zk;

    /*
    @Autowired
    @Qualifier("factory_with_ZK")
    private StateMachineFactory<States, Events> factory_with_zk;
    @Autowired
    private StateMachineEnsemble<States, Events> stateMachineEnsemble;
     */

    @Autowired
    private ServiceGateway serviceGateway;

    @Autowired
    private CuratorFramework sharedCuratorClient;

    private StateMachine<States, Events> stateMachine;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private ArrayList<Response> mixedCkpts;
    private ArrayList<Response> sequentialCktps;


    @Autowired
    private Environment environment;
    private Integer numberOfReplicas;
    private String solutionType;
    private Integer numberOfProcessedEvents;

    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @NotNull
        @SuppressWarnings("rawtypes")
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
            kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
            kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
            return kryo;
        }
    };


    private Dictionary event_eventNumber;

    static final Logger logger = LoggerFactory.getLogger(StateMachineWorker.class);

    public StateMachineWorker(){
        logger.info("+++++StateMachineWorker::Constructor+++++");
    }

    @PostConstruct
    public void init() {
        logger.info("+++++StateMachineWorker::PostConstruct+++++");

        /*
        stateMachine = factory_with_zk.getStateMachine();
        logger.info("UUID from factory_with_zk is {}",stateMachine.getUuid());
        stateMachine.start();
        stateMachineEnsemble.join(stateMachine);
         */


        stateMachine = factory_without_zk.getStateMachine();
        logger.info("UUID from factory_without_zk is {}",stateMachine.getUuid());
        stateMachine.start();
        mixedCkpts = new ArrayList<Response>();
        sequentialCktps = new ArrayList<Response>();


        logger.info("SMOC __{}__ is started. From now on, events can be processed.",stateMachine.getUuid().toString());
        event_eventNumber = new Hashtable();
        numberOfReplicas = Integer.valueOf(environment.getProperty("smoc.replicas"));
        solutionType = environment.getProperty("smoc.solutionType");
        logger.info("Experimental setup for ___{}___ ckpt structure with ___{}___ smocs",solutionType, numberOfReplicas);
        /*Registers an exit hook which runs when the JVM is shut down*/
        logger.info("Registers an exit hook which runs when the JVM is shut down.");
        InputStream stream = System.in;
        Scanner scanner = new Scanner(stream);
        Runtime.getRuntime().addShutdownHook(new ExitHook(this.stateMachine,scanner));

        numberOfProcessedEvents = 0;

    }

    public String ProcessEvent(@NotNull String event, Integer eventNumber, int timeSleep, boolean ckpt) throws Exception {
        Message<String> reply = null;
        switch(event){
            case "Pay": case "pay": case "PAY":
                logger.info("***************");
                logger.info("***************");
                reply = sendPayEvent(event, eventNumber,timeSleep, ckpt);
                logger.info("***************");
                logger.info("***************");
                break;
            case "Receive": case "receive": case "RECEIVE":
                logger.info("***************");
                logger.info("***************");
                reply = sendReceiveEvent(event, eventNumber,timeSleep, ckpt);
                logger.info("***************");
                logger.info("***************");
                break;
            case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                logger.info("***************");
                logger.info("***************");
                reply = sendStartFromScratchEvent(event, eventNumber,timeSleep, ckpt);
                logger.info("***************");
                logger.info("***************");
                break;
            default:
                System.out.println("Please send one of the events below.");
                System.out.println("Pay/Receive/StartFromScratch");
                break;
        }
        return reply.getPayload();

    }

    public Message<String> sendPayEvent(@NotNull String event, Integer eventNumber, int timeSleep, boolean willCkptTriggered) throws Exception {
        //numberOfEvents = numberOfEvents + 1;
        logger.info("{}.event will be processed",eventNumber);
        event_eventNumber.put(eventNumber,event);

        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "UNPAID")
                .setHeader("processedEvent", event)
                .setHeader("target","WAITING_FOR_RECEIVE")
                .build();
        stateMachine.sendEvent(messagePay);
        logger.info("Current variables : {}",stateMachine.getExtendedState().getVariables());
        logger.info("AFTER EVENT {} : Previous state = {}, Current state = {}",Events.PAY.toString(),"UNPAID","WAITING_FOR_RECEIVE");

        /*
        if (numberOfEvents < 2 ){
            logger.info("Number of events processed by this SMOC is {}. No need to persist a CKPT.",numberOfEvents);
        }
        */


        /* Prepare message for CKPT */
        Message<String> ckptMessage = MessageBuilder
                    //.withPayload("PAY")
                    .withPayload(eventNumber+","+  System.getenv("HOSTNAME")+","+"UNPAID"+","+event+","+"WAITING_FOR_RECEIVE")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "UNPAID")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "WAITING_FOR_RECEIVE")
                    .setHeader("context", serializeStateMachineContext())
                   //.setHeader("context", new String(serializeStateMachineContext()))
                    .setHeader("eventNumber",eventNumber)
                    .build();
        /*
        Stores on mongodb database
        serviceGateway.setCheckpoint(ckptMessage);
        */

        /*
        logger.info("Mark CKPT in the Zookeeper");
        MarkCKPT();
        */


        switch(solutionType){
            case "centralized": case "Centralized":
                /* Do not store CKPTs locally */
                logger.info("WillCheckpoint be triggered:{} BUT Centralized CKPTing, do not store locally",willCkptTriggered);
                numberOfProcessedEvents = numberOfProcessedEvents + 1;
                logger.info("Processed = {}",numberOfProcessedEvents);
                break;
            case "distributed": case "Distributed":
                /*Store CKPT locally */
                if(willCkptTriggered) {
                    logger.info("WillCheckpoint be triggered:{},Distributed CKPTing, stores locally",willCkptTriggered);
                    serviceGateway.storeCKPTInMemory(ckptMessage);
                }
                else{
                    logger.info("WillCheckpoint be triggered:{},Distributed CKPTing, does not store locally",willCkptTriggered);
                }
                break;
            case "mirrored": case "MIRRORED":
                /*Store CKPT locally */
                if(willCkptTriggered) {
                    logger.info("WillCheckpoint be triggered:{},Mirrored CKPTing, stores locally",willCkptTriggered);
                    serviceGateway.storeCKPTInMemory(ckptMessage);
                }
                else{
                    logger.info("WillCheckpoint be triggered:{},Mirrored CKPTing, does not store locally",willCkptTriggered);
                }
                break;

            case "conventional": case "Conventional":
                /*Store CKPT locally */
                logger.info("WillCheckpoint be triggered:{},Conventional CKPTing, all smocs stores locally all CKPTs",willCkptTriggered);
                serviceGateway.storeCKPTInMemory(ckptMessage);
                break;
        }

        /* Send CKPT in order to stored externally, if it is needed */
        return ckptMessage;

    }

    public Message<String> sendReceiveEvent(@NotNull String event, Integer eventNumber, int timeSleep, boolean willCkptTriggered) throws Exception {
        //numberOfEvents = numberOfEvents + 1;
        logger.info("{}.event will be processed",eventNumber);
        event_eventNumber.put(eventNumber,event);

        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "WAITING_FOR_RECEIVE")
                .setHeader("processedEvent", event)
                .setHeader("target", "DONE")
                .build();
        stateMachine.sendEvent(messageReceive);
        logger.info("Current variables : {}",stateMachine.getExtendedState().getVariables());
        logger.info("AFTER EVENT {} : Previous state = {}, Current state = {}",Events.RECEIVE.toString(),"WAITING_FOR_RECEIVE","DONE");

        /*
        if (numberOfEvents < 2 ){
           logger.info("Number of events processed by this SMOC is {}. No need to persist a CKPT",numberOfEvents);
        }
        */
        Message<String> ckptMessage = MessageBuilder
                    //.withPayload("RCV")
                    .withPayload(eventNumber+","+  System.getenv("HOSTNAME")+","+"WAITING_FOR_RECEIVE"+","+event+","+"DONE")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "WAITING_FOR_RECEIVE")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "DONE")
                    .setHeader("context", serializeStateMachineContext())
                    //.setHeader("context", new String(serializeStateMachineContext()))
                    .setHeader("eventNumber",eventNumber)
                    .build();
        /*
        Stores on mongodb database
        serviceGateway.setCheckpoint(ckptMessage);
         */

        /*
        logger.info("Mark CKPT in the Zookeeper");
        MarkCKPT();
         */

        switch(solutionType){
            case "centralized": case "Centralized":
                /* Do not store CKPTs locally */
                logger.info("WillCheckpoint be triggered:{} BUT Centralized CKPTing, do not store locally",willCkptTriggered);
                numberOfProcessedEvents = numberOfProcessedEvents + 1;
                logger.info("Processed = {}",numberOfProcessedEvents);
                break;
            case "distributed": case "Distributed":
                /*Store CKPT locally */
                if(willCkptTriggered) {
                    logger.info("WillCheckpoint be triggered:{},Distributed CKPTing, stores locally",willCkptTriggered);
                    serviceGateway.storeCKPTInMemory(ckptMessage);
                }
                else{
                    logger.info("WillCheckpoint be triggered:{},Distributed CKPTing, does not store locally",willCkptTriggered);
                }
                break;
            case "mirrored": case "MIRRORED":
                /*Store CKPT locally */
                if(willCkptTriggered) {
                    logger.info("WillCheckpoint be triggered:{},Mirrored CKPTing, stores locally",willCkptTriggered);
                    serviceGateway.storeCKPTInMemory(ckptMessage);
                }
                else{
                    logger.info("WillCheckpoint be triggered:{},Mirrored CKPTing, does not store locally",willCkptTriggered);
                }
                break;
            case "conventional": case "Conventional":
                /*Store CKPT locally */
                logger.info("WillCheckpoint be triggered:{},Conventional CKPTing, all smocs stores locally all CKPTs",willCkptTriggered);
                serviceGateway.storeCKPTInMemory(ckptMessage);
                break;
        }

        return ckptMessage;

    }

    public Message<String> sendStartFromScratchEvent(@NotNull String event, Integer eventNumber, int timeSleep, boolean willCkptTriggered) throws Exception {
        //numberOfEvents = numberOfEvents + 1;
        logger.info("{}.event will be processed",eventNumber);
        event_eventNumber.put(eventNumber,event);

        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "DONE")
                .setHeader("processedEvent", event)
                .setHeader("target","UNPAID")
                .build();
        stateMachine.sendEvent(messageStartFromScratch);
        logger.info("Current variables : {}",stateMachine.getExtendedState().getVariables());
        logger.info("AFTER EVENT {} : Previous state = {}, Current state = {}",Events.STARTFROMSCRATCH.toString(),"DONE","UNPAID");

        /*
        if (numberOfEvents < 2 ){
           logger.info("Number of events processed by this SMOC is {}. No need to persist a CKPT.",numberOfEvents);
        }
        */
        //numberOfEvents = 0;
        Message<String> ckptMessage = MessageBuilder
                    //.withPayload("SFS")
                    .withPayload(eventNumber+","+  System.getenv("HOSTNAME")+","+"DONE"+","+event+","+"UNPAID")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "DONE")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "UNPAID")
                    .setHeader("context", serializeStateMachineContext())
                    //.setHeader("context", new String(serializeStateMachineContext()))
                    .setHeader("eventNumber",eventNumber)
                    .build();

        /*
        Stores on mongodb database
        serviceGateway.setCheckpoint(ckptMessage);
         */

         /*
        logger.info("Mark CKPT in the Zookeeper");
        MarkCKPT();
         */

        switch(solutionType){
            case "centralized": case "Centralized":
                /* Do not store CKPTs locally */
                logger.info("WillCheckpoint be triggered:{} BUT Centralized CKPTing, do not store locally",willCkptTriggered);
                numberOfProcessedEvents = numberOfProcessedEvents + 1;
                logger.info("Processed = {}",numberOfProcessedEvents);
                break;
            case "distributed": case "Distributed":
                /*Store CKPT locally */
                if(willCkptTriggered) {
                    logger.info("WillCheckpoint be triggered:{},Distributed CKPTing, stores locally",willCkptTriggered);
                    serviceGateway.storeCKPTInMemory(ckptMessage);
                }
                else{
                    logger.info("WillCheckpoint be triggered:{},Distributed CKPTing, does not store locally",willCkptTriggered);
                }
                break;
            case "mirrored": case "MIRRORED":
                /*Store CKPT locally */
                if(willCkptTriggered) {
                    logger.info("WillCheckpoint be triggered:{},Mirrored CKPTing, stores locally",willCkptTriggered);
                    serviceGateway.storeCKPTInMemory(ckptMessage);
                }
                else{
                    logger.info("WillCheckpoint be triggered:{},Mirrored CKPTing, does not store locally",willCkptTriggered);
                }
                break;
            case "conventional": case "Conventional":
                /*Store CKPT locally */
                logger.info("WillCheckpoint be triggered:{},Conventional CKPTing, all smocs stores locally all CKPTs",willCkptTriggered);
                serviceGateway.storeCKPTInMemory(ckptMessage);
                break;
        }

        return ckptMessage;
    }

    public  String serializeStateMachineContext(){
        /*
        Kryo kryo = kryoThreadLocal.get();
        Base64.Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        kryo.writeObject(output, context);
        output.close();
        byte[] byteArr = baos.toByteArray();
        //logger.info("bytearray = {}",byteArr);
        String str_with_UTF_8 = new String(byteArr, StandardCharsets.UTF_8);
        //logger.info("str_with_UTF_8 = {}",str_with_UTF_8);
        //logger.info("bytes_with_UTF_8 = {}",str_with_UTF_8.getBytes());
        String str_with_ISO = new String(byteArr, StandardCharsets.ISO_8859_1);
        //logger.info("str_with_ISO = {}",str_with_ISO);
        //logger.info("bytes_with_ISO = {}",str_with_ISO.getBytes());
        // daha sonra tekrar byte[] <- String
        String serializedContext = encoder.encodeToString(byteArr);
        //logger.warn("Serialized context = {}",serializedContext);
        return serializedContext;
        //return output.toString();
        //return baos.toString();
        //return baos.toByteArray();
         */
        return "MOCK_SMOC_CONTEXT";
    }

    public void MarkCKPT() throws Exception {
        /* Get processed events */
        String str_data = "";
        for (Enumeration keys = event_eventNumber.keys(); keys.hasMoreElements();)
        {
            Integer key = (Integer) keys.nextElement();
            String value = event_eventNumber.get(key).toString();
            logger.info("Eventnumber {} belongs to event {}",key,value);
            //str_data = str_data + key + "-->"+ value + "...";
            str_data = str_data + key + ",";
        }
        /*Read hostname from env */
        String hostname = System.getenv("HOSTNAME");
        //String str_data = "CKPT information:" + getTimeStamp() + "__"  + hostname ;
        byte[] data = str_data.getBytes();
        String path = "/" + hostname;
        if(sharedCuratorClient.checkExists().forPath(path)!=null) {
            //node exists
            logger.info("++++++++++ PATH {} EXISTS ++++++++++",path);
            byte[] bytes = sharedCuratorClient.getData().forPath(path);
            logger.info("+++++++++ Current data in zkNode =  {} ++++++++++",new String(bytes));
            logger.info("+++++++++ zkNode will be changed ++++++++++");
            sharedCuratorClient.setData().forPath(path, data);
        } else {
            //node does not exist, create new zknode
            logger.info("+++++++++ PATH {} DOES NOT EXIST, WILL BE CREATED FOR NOW ++++++++++",path);
            sharedCuratorClient.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(path, data);
        }
    }

    public String getTimeStamp(){
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int ms = now.get(Calendar.MILLISECOND);

        String ts = year + "." + month + "." +  day + "_" + hour + "." + minute + "." + second + "." + ms;
        return ts;
    }

    public void startCommunication(String solutionType) throws UnknownHostException {
        logger.info("********* StateMachineWorker::startCommunication()");
        String ipAddr = InetAddress.getLocalHost().getHostAddress();
        String hostname = System.getenv("HOSTNAME");
        logger.info("********* Ip Addr of sender = {}", ipAddr);
        logger.info("********* Hostname of sender  = {}", hostname);
        CkptMessage msg = new CkptMessage();
        msg.setHostname(hostname);
        msg.setIpAddr(ipAddr);


        switch (solutionType){
            case "centralized": case "Centralized":
                sequentialCktps = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("LB_EXCHANGE","rpc",msg);
                logger.info("Count of ckpts stored by loadbalancer --> {}",sequentialCktps.size());
                break;
            case "distributed": case "Distributed":
                for(int smoc=0; smoc < numberOfReplicas; smoc ++){
                    /* Construct the exchange, in this way: SMOC1_CKPT_EXCHANGE, SMOC2_CKPT_EXCHANGE, so on */
                    Integer index = smoc + 1;
                    String exchange = "SMOC" + (index) +"_CKPT_EXCHANGE";
                    logger.info("Exchange is = {}",exchange);
                    ArrayList<Response> smocCkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive(exchange,"rpc",msg);
                    logger.info("Size of list is = {}",smocCkptList.size());
                    mixedCkpts.addAll(smocCkptList);
                }
                logger.info("Count of ckpts stored by all smocs --> {}",mixedCkpts.size());
                break;
            case "conventional": case "Conventional":
                sequentialCktps = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC1_CKPT_EXCHANGE","rpc",msg);
                logger.info("Count of ckpts stored by any of smocs, e.g.: smoc1 --> {}",sequentialCktps.size());
                break;
        }



    }

    public void prepareCkpts(){

        Integer size = mixedCkpts.size();

        for(int event=1 ; event<=size; event++){
            logger.info("Searching for eventnumber {} is started ...",event);
            for(Response response: mixedCkpts){
                if(response.getEventNumber() == event){
                    logger.info("Eventnumber {} is found",event);
                    sequentialCktps.add(response);
                }
            }
            logger.info("Searching for event {} is finished ...",event);
        }
        logger.info("Size of ordered ckpts -> {}",sequentialCktps.size());

        for(Response response:sequentialCktps){
            logger.warn("{}.event: {} --> {} --> {}",response.getEventNumber(),response.getSourceState(),response.getProcessedEvent(),response.getDestinationState());
        }

    }

    public void applyCkpts() throws Exception {
        logger.info("********* StateMachineWorker::applyCkpts()");
        for(Response response: sequentialCktps){
            String event = response.getProcessedEvent();
            Integer eventNumber = response.getEventNumber();
            switch(event){
                case "Pay": case "pay": case "PAY":
                    System.out.print("\n\n\n\n\n");
                    sendPayEvent(event, eventNumber,0,true);
                    System.out.print("\n\n\n\n\n");
                    break;
                case "Receive": case "receive": case "RECEIVE":
                    System.out.print("\n\n\n\n\n");
                    sendReceiveEvent(event, eventNumber,0, true);
                    System.out.print("\n\n\n\n\n");
                    break;
                case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                    System.out.print("\n\n\n\n\n");
                    sendStartFromScratchEvent(event, eventNumber,0, true);
                    System.out.print("\n\n\n\n\n");
                    break;
                default:
                    System.out.println("Please send one of the events below.");
                    System.out.println("Pay/Receive/StartFromScratch");
                    break;
            }
        }
    }
}

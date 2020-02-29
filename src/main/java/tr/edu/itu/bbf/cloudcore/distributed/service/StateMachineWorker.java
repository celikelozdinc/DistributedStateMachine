package tr.edu.itu.bbf.cloudcore.distributed.service;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
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
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import tr.edu.itu.bbf.cloudcore.distributed.ipc.CkptMessage;
import tr.edu.itu.bbf.cloudcore.distributed.ipc.Response;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

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
        logger.info("Experimental setup with ___{}___ smocs",numberOfReplicas);
        /*Registers an exit hook which runs when the JVM is shut down*/
        logger.info("Registers an exit hook which runs when the JVM is shut down.");
        InputStream stream = System.in;
        Scanner scanner = new Scanner(stream);
        Runtime.getRuntime().addShutdownHook(new ExitHook(this.stateMachine,scanner));


    }

    public String ProcessEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
        Message<String> reply = null;
        switch(event){
            case "Pay": case "pay": case "PAY":
                System.out.print("\n\n\n\n\n");
                reply = sendPayEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            case "Receive": case "receive": case "RECEIVE":
                System.out.print("\n\n\n\n\n");
                reply = sendReceiveEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                System.out.print("\n\n\n\n\n");
                reply = sendStartFromScratchEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            default:
                System.out.println("Please send one of the events below.");
                System.out.println("Pay/Receive/StartFromScratch");
                break;
        }
        return reply.getPayload();

    }

    public Message<String> sendPayEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
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

        /*Store CKPT locally */
        //serviceGateway.storeCKPTInMemory(ckptMessage);

        /* Send CKPT in order to stored externally */
        return ckptMessage;

    }

    public Message<String> sendReceiveEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
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

        /*Store CKPT locally */
        //serviceGateway.storeCKPTInMemory(ckptMessage);

        /* Send CKPT in order to stored externally */
        return ckptMessage;

    }

    public Message<String> sendStartFromScratchEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
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

        /*Store CKPT locally */
        //serviceGateway.storeCKPTInMemory(ckptMessage);

        /* Send CKPT in order to stored externally */
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
                    String exchange = "SMOC" + (smoc + 1) +"_CKPT_EXCHANGE";
                    ArrayList<Response> smocCkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive(exchange,"rpc",msg);
                    mixedCkpts.addAll(smocCkptList);
                }
                logger.info("Count of ckpts stored by all smocs --> {}",mixedCkpts.size());
                break;
        }

        /*
        ArrayList<Response> smoc1CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC1_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc1 --> {}",smoc1CkptList.size());
        mixedCkpts.addAll(smoc1CkptList);
        ArrayList<Response> smoc2CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC2_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc2 --> {}",smoc2CkptList.size());
        mixedCkpts.addAll(smoc2CkptList);
        ArrayList<Response> smoc3CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC3_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc3 --> {}",smoc3CkptList.size());
        mixedCkpts.addAll(smoc3CkptList);
        ArrayList<Response> smoc4CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC4_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc4 --> {}",smoc4CkptList.size());
        mixedCkpts.addAll(smoc4CkptList);
        ArrayList<Response> smoc5CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC5_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc5 --> {}",smoc5CkptList.size());
        mixedCkpts.addAll(smoc5CkptList);
        ArrayList<Response> smoc6CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC6_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc6 --> {}",smoc6CkptList.size());
        mixedCkpts.addAll(smoc6CkptList);
        ArrayList<Response> smoc7CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC7_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc7 --> {}",smoc7CkptList.size());
        mixedCkpts.addAll(smoc7CkptList);
        ArrayList<Response> smoc8CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC8_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc8 --> {}",smoc8CkptList.size());
        mixedCkpts.addAll(smoc8CkptList);
        ArrayList<Response> smoc9CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC9_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc9 --> {}",smoc9CkptList.size());
        mixedCkpts.addAll(smoc9CkptList);
        ArrayList<Response> smoc10CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC10_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc10 --> {}",smoc10CkptList.size());
        mixedCkpts.addAll(smoc10CkptList);
        ArrayList<Response> smoc11CkptList = (ArrayList<Response>) rabbitTemplate.convertSendAndReceive("SMOC11_CKPT_EXCHANGE","rpc",msg);
        logger.info("Count of ckpts stored by smoc11 --> {}",smoc11CkptList.size());
        mixedCkpts.addAll(smoc11CkptList);
         logger.info("Count of ckpts stored by all smocs --> {}",mixedCkpts.size());
        */



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
                    sendPayEvent(event, eventNumber,0);;
                    System.out.print("\n\n\n\n\n");
                    break;
                case "Receive": case "receive": case "RECEIVE":
                    System.out.print("\n\n\n\n\n");
                    sendReceiveEvent(event, eventNumber,0);;
                    System.out.print("\n\n\n\n\n");
                    break;
                case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                    System.out.print("\n\n\n\n\n");
                    sendStartFromScratchEvent(event, eventNumber,0);;
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

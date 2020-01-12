package tr.edu.itu.bbf.cloudcore.distributed.service;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @Autowired
    private StateMachineEnsemble<States, Events> stateMachineEnsemble;

    @Autowired
    private ServiceGateway serviceGateway;

    @Autowired
    private CuratorFramework sharedCuratorClient;

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

    //private Integer numberOfEvents;

    private Dictionary event_eventNumber;

    static final Logger logger = LoggerFactory.getLogger(StateMachineWorker.class);

    public StateMachineWorker(){
        logger.info("+++++StateMachineWorker::Constructor+++++");
    }

    @PostConstruct
    public void init() {
        logger.info("+++++StateMachineWorker::PostConstruct+++++");
        stateMachine.start();
        stateMachineEnsemble.join(stateMachine);
        logger.info("SMOC __{}__ is started. From now on, events can be processed.",stateMachine.getUuid().toString());
        //numberOfEvents = 0;
        //logger.info("# of events for smoc __{}__ is initialized to = __{}__",stateMachine.getUuid().toString(),numberOfEvents);
        event_eventNumber = new Hashtable();
        /*Registers an exit hook which runs when the JVM is shut down*/
        logger.info("Registers an exit hook which runs when the JVM is shut down.");
        InputStream stream = System.in;
        Scanner scanner = new Scanner(stream);
        Runtime.getRuntime().addShutdownHook(new ExitHook(stateMachine,scanner));
    }

    public void ProcessEvent(String event, Integer eventNumber, int timeSleep) throws Exception {
        switch(event){
            case "Pay": case "pay": case "PAY":
                System.out.print("\n\n\n\n\n");
                sendPayEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            case "Receive": case "receive": case "RECEIVE":
                System.out.print("\n\n\n\n\n");
                sendReceiveEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                System.out.print("\n\n\n\n\n");
                sendStartFromScratchEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            default:
                System.out.println("Please send one of the events below.");
                System.out.println("Pay/Receive/StartFromScratch");
                break;
        }

    }

    public void sendPayEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
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
                    .withPayload("PAY")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "UNPAID")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "WAITING_FOR_RECEIVE")
                    .setHeader("context", serializeStateMachineContext())
                    .build();
        serviceGateway.setCheckpoint(ckptMessage);
        /*
        logger.info("Mark CKPT in the Zookeeper");
        MarkCKPT();
        */

    }

    public void sendReceiveEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
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
                    .withPayload("RCV")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "WAITING_FOR_RECEIVE")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "DONE")
                    .setHeader("context", serializeStateMachineContext())
                    .build();
        serviceGateway.setCheckpoint(ckptMessage);
        /*
        logger.info("Mark CKPT in the Zookeeper");
        MarkCKPT();
         */
    }

    public void sendStartFromScratchEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
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
                    .withPayload("SFS")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "DONE")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "UNPAID")
                    .setHeader("context", serializeStateMachineContext())
                    .build();
        serviceGateway.setCheckpoint(ckptMessage);
        /*
        logger.info("Mark CKPT in the Zookeeper");
        MarkCKPT();
         */
    }

    public  String serializeStateMachineContext(){
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        kryo.writeObject(output, context);
        output.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /*
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
     */

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

}

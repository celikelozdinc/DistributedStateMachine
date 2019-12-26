package tr.edu.itu.bbf.cloudcore.distributed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import tr.edu.itu.bbf.cloudcore.distributed.ipc.Sender;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.service.ServiceGateway;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ImportResource({"classpath*:channel-config.xml"})
@PropertySource(value={"classpath:application.properties"})
@ComponentScan(basePackages = {"tr.edu.itu.bbf.cloudcore.distributed"})
@EnableMongoRepositories(basePackageClasses=CheckpointRepository.class)
public class Application implements CommandLineRunner {

    static class ExitHook extends Thread {

        @Autowired
        private StateMachine<States, Events> stateMachine;
        private Scanner scanner;


        public ExitHook(StateMachine<States,Events> sm, Scanner sc){
            this.stateMachine = sm;
            this.scanner = sc;
        }

        @Override
        public void run(){
            System.out.println("*****Gracefully stopping SMOC*****");
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

    @Autowired
    private Sender sender;

    private Integer numberOfEvents;


    @Override
    public void run(String... args) throws Exception {
        /* Reads timesleep argument
        *  which reads from `Program Arguments` section in jar configuration
        *String[] argument = args[0].split("=");
        *int timeSleep = Integer.parseInt(argument[1]);
        */

        /* Reads named arguments from `VM Options` section in jar configuration */
        int timeSleep = Integer.parseInt(System.getProperty("timesleep"));
        /*
        String type = System.getProperty( "type" );
        System.out.println("TYPE of SMOC is: " + type);
         */

        stateMachine.start();
        stateMachineEnsemble.join(stateMachine);

        InputStream stream = System.in;
        Scanner scanner = new Scanner(stream);
        /*
        Registers an exit hook
        which starts when the JVM is shut down
        */
        Runtime.getRuntime().addShutdownHook(new ExitHook(stateMachine,scanner));
        System.out.printf("SMOC %s is started. From now on, you can send events.\n",stateMachine.getUuid().toString());

        numberOfEvents = 0;
        System.out.printf("# of events for this SMOC is initialized to = %d\n",numberOfEvents);

        ApplicationContext context = new ClassPathXmlApplicationContext("channel-config.xml");
        serviceGateway = (ServiceGateway) context.getBean("serviceGateway");

        /* Read hostname from ENV of SMOC */
        String hostname = System.getenv("HOSTNAME");
        if (hostname.equals("smoc4")){
            String exchange = "";
            String reply = "";
            System.out.println(" --------------------------------");
            System.out.println("SMOC4 will try to make RPC call to other smocs.");
            // Hangi smoc'tan CKPT isteyeceğine karar ver.
            // Sonra, onun exchange ile send çağrısı yap
            exchange="SMOC2_CKPT_EXCHANGE";
            reply = sender.send(exchange);
            System.out.println("********* Response from receiver = " + reply);
            System.out.println(" --------------------------------");
            exchange="SMOC3_CKPT_EXCHANGE";
            reply = sender.send(exchange);
            System.out.println("********* Response from receiver = " + reply);
            System.out.println(" --------------------------------");
        }

        try {
            while (true) {
                System.out.print("Event:");
                String event = scanner.next();
                System.out.println("This event will be processed: " + event);
                ProcessEvent(event, timeSleep);
                sleep((long) 5);
                MarkCheckpoint();
                ReadMarkedCheckpoints();
                PrintCurrentStatus();
                // Can get, but can not set extendedstate variables
            }
        }catch(IllegalStateException e) {
            System.out.println("Exiting main loop...");
            e.printStackTrace();
        }
        /*
        if (type.equals("sender")){
            sendPayEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after Pay event for sender is finished.");
            sendStartFromScratchEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after StartFromScratch event for sender is finished.");
            sendReceiveEvent(timeSleep);
            System.out.println("*****TIMESLEEP after Receive event for sender is finished.");
        }
        else if (type.equals("receiver")){
            sendReceiveEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after Receive event for receiver is finished.");
            sendPayEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after Pay event for receiver is finished.");
            sendStartFromScratchEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after StartFromScratch event for receiver is finished.");
        }
        */
        /* LOOP 1*/
        /*
        sendPayEvent(timeSleep);
        sync(type);
        sendReceiveEvent(timeSleep);
        sendStartFromScratchEvent(timeSleep);
         */
        /* LOOP 2*/
        /*
        sendPayEvent(timeSleep);
        sendReceiveEvent(timeSleep);
        sync(type);
        sendStartFromScratchEvent(timeSleep);
         */
        /* LOOP 3 */
        /*
        sendPayEvent(timeSleep);
        sync(type);
        sendReceiveEvent(timeSleep);
        sync(type);
        sendStartFromScratchEvent(timeSleep);
         */
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void PrintCurrentStatus() throws Exception{
        /* Read from ensemble */
        System.out.println("PrintCurrentStatus()::READ FROM ENSEMBLE");
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        System.out.println("PROCESSED EVENT IS " + context.getEvent().toString());
        System.out.println("AFTER EVENT, STATE IS " + context.getState().toString());
        System.out.println("AFTER EVENT, common and local variables are below:");
        ExtendedState  extendedState = context.getExtendedState();
        System.out.println("Common variable between events: " + extendedState.get("common", Integer.class) );
        System.out.println("Local variable for Waiting State: " + extendedState.get("localVarForWaiting", Integer.class));
        System.out.println("Local variable for Done State: " + extendedState.get("localVarForDone",Integer.class));
        /* Read from mongodb database */
        System.out.println("PrintCurrentStatus()::READ FROM MONGODB");
        Message<String> getMessage = MessageBuilder
                .withPayload("PAYLOAD")
                .build();
        List<CheckpointDbObject> list = serviceGateway.getCheckpoint(getMessage);
        /* If list is not empty, iterate over list*/
        if (list != null && !list.isEmpty()) {
            System.out.println("# checkpoints inserted on database = " + list.size());
            for (CheckpointDbObject dbObject : list) {
                System.out.printf("Source state: %s\n", dbObject.getSourceState());
                System.out.printf("Processed event: %s\n", dbObject.getProcessedEvent());
                System.out.printf("Target state: %s\n", dbObject.getTargetState());
            }
        }
        /* IPC operations
        System.out.println(" ********* RPC STARTED *********");
        String reply = sender.send();
        System.out.println("********* Response from receiver = " + reply);
        System.out.println(" ********* RPC FINISHED *********");
         */

    }

    public void sendPayEvent(@NotNull String event, int timeSleep){
        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "UNPAID")
                .setHeader("processedEvent", event)
                .setHeader("target","WAITING_FOR_RECEIVE")
                .build();
        stateMachine.sendEvent(messagePay);

        if (numberOfEvents < 3 ){
            System.out.printf("Number of events processed by this SMOC is %d. No need to persist a CKPT.",numberOfEvents);
        }
        else {
            System.out.printf("Number of events processed by this SMOC is %d. Persist a CKPT, initialize counter again.",numberOfEvents);
            numberOfEvents = 0;
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
        }
    }
    public void sendReceiveEvent(@NotNull String event,int timeSleep){
        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "WAITING_FOR_RECEIVE")
                .setHeader("processedEvent", event)
                .setHeader("target", "DONE")
                .build();
        stateMachine.sendEvent(messageReceive);

        if (numberOfEvents < 3 ){
            System.out.printf("Number of events processed by this SMOC is %d. No need to persist a CKPT.\n",numberOfEvents);
        }
        else {
            System.out.printf("Number of events processed by this SMOC is %d. Persist a CKPT, initialize counter again.\n", numberOfEvents);
            numberOfEvents = 0;
            /* Prepare message for CKPT */
            Message<String> ckptMessage = MessageBuilder
                    .withPayload("RCV")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "WAITING_FOR_RECEIVE")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "DONE")
                    .setHeader("context", serializeStateMachineContext())
                    .build();
            serviceGateway.setCheckpoint(ckptMessage);
        }
    }
    public void sendStartFromScratchEvent(@NotNull String event,int timeSleep){
        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "DONE")
                .setHeader("processedEvent", event)
                .setHeader("target","UNPAID")
                .build();
        stateMachine.sendEvent(messageStartFromScratch);

        if (numberOfEvents < 3 ){
            System.out.printf("Number of events processed by this SMOC is %d. No need to persist a CKPT.",numberOfEvents);
        }
        else {
            System.out.printf("Number of events processed by this SMOC is %d. Persist a CKPT, initialize counter again.", numberOfEvents);
            numberOfEvents = 0;
            /* Prepare message for CKPT */
            Message<String> ckptMessage = MessageBuilder
                    .withPayload("SFS")
                    .setHeader("machineId", stateMachine.getUuid())
                    .setHeader("source", "DONE")
                    .setHeader("processedEvent", event)
                    .setHeader("target", "UNPAID")
                    .setHeader("context", serializeStateMachineContext())
                    .build();
            serviceGateway.setCheckpoint(ckptMessage);
        }
    }
    public void sleep(Long sleepTime){
        try {
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Exception during sleep in main program --> " + ex.toString());
        }

    }
    public void ProcessEvent(@NotNull String event, int timeSleep){
        switch(event){
            case "Pay":
                numberOfEvents ++;
                sendPayEvent(event, timeSleep);
                break;
            case "Receive":
                numberOfEvents ++;
                sendReceiveEvent(event, timeSleep);
                break;
            case "StartFromScratch":
                numberOfEvents ++;
                sendStartFromScratchEvent(event, timeSleep);
                break;
            default:
                System.out.println("Please send one of the events below.");
                System.out.println("Pay/Receive/StartFromScratch");
                break;
        }

    }
    public String serializeStateMachineContext(){
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        kryo.writeObject(output, context);
        output.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
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

    @Bean
    public CuratorFramework sharedCuratorClient() throws Exception {
        String zkConnectionString = "zookeeper:2181";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .defaultData(new byte[0])
                .retryPolicy(retryPolicy)
                .connectString(zkConnectionString)
                .build();
        client.start();
        CuratorFrameworkState state = client.getState();
        System.out.println("**********************");
        System.out.println("**********************");
        System.out.println("***********sharedCuratorClient state after initialization ----> " + state.name());
        System.out.println("**********************");
        System.out.println("**********************");
        return client;
    }

    public void MarkCheckpoint() throws Exception {
        InetAddress inetAddress = InetAddress.getLocalHost();
        String ipAddress = inetAddress.getHostAddress();
        String hostname = inetAddress.getHostName();
        String str_data = "CKPT information:" + getTimeStamp() + "__" + ipAddress +"__" + hostname ;
        byte[] data = str_data.getBytes();
        String path = "/" + hostname;
        if(sharedCuratorClient.checkExists().forPath(path)!=null) {
            //node exists
            System.out.println(path+"  : EXISTS*********");
            byte[] bytes = sharedCuratorClient.getData().forPath(path);
            System.out.println("----------zkNode data:" + new String(bytes) + "------------");
            System.out.println("----------zkNode will be changed.");
            sharedCuratorClient.setData().forPath(path, data);
        } else {
            //node does not exist, create new
            System.out.println(path+ "  : DOES NOT EXIST*********");
            sharedCuratorClient.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(path, data);
        }

    }

    public void ReadMarkedCheckpoints() throws Exception {
        List<String> paths = new ArrayList<String>();
        paths.add("/smoc1");
        paths.add("/smoc2");
        paths.add("/smoc3");
        paths.add("/smoc3");
        for(String path: paths) {
            if(sharedCuratorClient.checkExists().forPath(path)!=null) {
                byte[] bytes = sharedCuratorClient.getData().forPath(path);
                System.out.println("---ReadMarkedCheckpoints()::" + "PATH="+ path + "::DATA=" + new String(bytes));
            }

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


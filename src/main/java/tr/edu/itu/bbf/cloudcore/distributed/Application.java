package tr.edu.itu.bbf.cloudcore.distributed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.context.ApplicationContext;
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
import tr.edu.itu.bbf.cloudcore.distributed.config.ServiceGateway;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.Scanner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
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

    private ServiceGateway serviceGateway;

    /*@Autowired
    private ChckpointPersistenceService persistenceService;
     */

    /*
    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;
     */

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
        which start when the JVM is shut down
        */

        //directChannel.subscribe(new __Subscriber());

        Runtime.getRuntime().addShutdownHook(new ExitHook(stateMachine,scanner));
        System.out.printf("SMOC %s is started. From now on, you can send events.\n",stateMachine.getUuid().toString());


        //persistenceService.persister.persist(stateMachine,stateMachine.getUuid());
        //CheckpointDbObject dbObject1 = new CheckpointDbObject(stateMachine.getUuid(),"EVENT1");
        //dbObjectHandler.insertCheckpoint(dbObject1);
        //CheckpointDbObject dbObject2 = new CheckpointDbObject(stateMachine.getUuid(),"EVENT2");
        //dbObjectHandler.insertCheckpoint(dbObject2);
        ApplicationContext context = new ClassPathXmlApplicationContext("channel-config.xml");
        serviceGateway = (ServiceGateway) context.getBean("helloWorldGateway");

        try {
            while (true) {
                /*System.out.println("PERSIST: ");
                stateMachinePersister.persist(stateMachine, stateMachine.getUuid());
                 */
                System.out.print("Event:");
                String event = scanner.next();
                System.out.println("This event will be processed: " + event);
                ProcessEvent(event, timeSleep);
                sleep((long) 5);
                PrintCurrentStatus();
                //ckptGateway.persist("HELLO");
                //CheckpointDbObject dbObject = new CheckpointDbObject("timestamp", serializeStateMachineContext());
                //dbObjectHandler.insertCheckpoint(dbObject);
                //persistenceService.persister.persist(stateMachine,stateMachine.getUuid());
                // Can get, but can not set extendedstate variables
            }
        }catch(IllegalStateException e) {
            System.out.println("Exiting with exception ....");
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

    public void PrintCurrentStatus() {
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        System.out.println("PROCESSED EVENT IS " + context.getEvent().toString());
        System.out.println("AFTER EVENT, STATE IS " + context.getState().toString());
        System.out.println("AFTER EVENT, common and local variables are below:");
        ExtendedState  extendedState = context.getExtendedState();
        System.out.println("Common variable between events: " + extendedState.get("common", Integer.class) );
        System.out.println("Local variable for Waiting State: " + extendedState.get("localVarForWaiting", Integer.class));
        System.out.println("Local variable for Done State: " + extendedState.get("localVarForDone",Integer.class));
        /*
        System.out.println("~~~~~CKPT REPORT~~~~~");
        Map<String, ___Checkpoint> checkpoints = (Map<String, ___Checkpoint>) extendedState.getVariables().get("CKPT");
        for(Map.Entry<String, ___Checkpoint> entry : checkpoints.entrySet()) {
            System.out.println("-----");
            System.out.println("Timestamp -> " + entry.getKey());
            System.out.println("Processed by -> " + entry.getValue().getUuid());
            System.out.println("# CKPTs: -> " + entry.getValue().getNumberOfCKPTs());
            System.out.println("Source State -> " +entry.getValue().getSourceState() );
            System.out.println("Processed event -> " + entry.getValue().getProcessedEvent());
            System.out.println("Target State -> " + entry.getValue().getTargetState());
            System.out.println("Common Var -> " + entry.getValue().getCommon());
            System.out.println("LocalVarForWaiting -> " + entry.getValue().getLocalVarForWaiting());
            System.out.println("LocalVarForDone -> " + entry.getValue().getLocalVarForDone());
            System.out.println("-----");
        }
         */
    }


    public void sendPayEvent(@NotNull String event, int timeSleep){
        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                //.setHeader("stateMachineContext", serializeStateMachineContext())
                .setHeader("source", "UNPAID")
                .setHeader("processedEvent", event)
                .setHeader("target","WAITING_FOR_RECEIVE")
                .build();
        stateMachine.sendEvent(messagePay);

        /* Prepare message for subscriber */
        serviceGateway.setCheckpoint(serializeStateMachineContext());
        //publisher.sendCheckpointInformation(stateMachine.getUuid(), "UNPAID",event, "WAITING_FOR_RECEIVE", serializeStateMachineContext());
        //CheckpointDbObject dbObject = new CheckpointDbObject(this.getTimeStamp(),stateMachine.getUuid(),"UNPAID",event,"WAITING_FOR_RECEIVE",serializeStateMachineContext());
        //CheckpointDbObject dbObject = new CheckpointDbObject(this.getTimeStamp(), serializeStateMachineContext());
        //CheckpointDbObjectHandler dbObjectHandler =  new CheckpointDbObjectHandler();
        //dbObjectHandler.insertCheckpoint(dbObject);
    }
    public void sendReceiveEvent(@NotNull String event,int timeSleep){
        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                //.setHeader("stateMachineContext", serializeStateMachineContext())
                .setHeader("source", "WAITING_FOR_RECEIVE")
                .setHeader("processedEvent", event)
                .setHeader("target", "DONE")
                .build();
        stateMachine.sendEvent(messageReceive);

        /* Prepare message for subscriber */
        //publisher.sendCheckpointInformation(stateMachine.getUuid(), "WAITING_FOR_RECEIVE",event, "DONE", serializeStateMachineContext());
    }
    public void sendStartFromScratchEvent(@NotNull String event,int timeSleep){
        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                //.setHeader("stateMachineContext", serializeStateMachineContext())
                .setHeader("source", "DONE")
                .setHeader("processedEvent", event)
                .setHeader("target","UNPAID")
                .build();
        stateMachine.sendEvent(messageStartFromScratch);

        /* Prepare message for subscriber */
        //publisher.sendCheckpointInformation(stateMachine.getUuid(), "DONE",event, "UNPAID", serializeStateMachineContext());
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
                sendPayEvent(event, timeSleep);
                break;
            case "Receive":
                sendReceiveEvent(event, timeSleep);
                break;
            case "StartFromScratch":
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
        System.out.println("Result of serialization --> " + Base64.getEncoder().encodeToString(baos.toByteArray()).getClass().getName());
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


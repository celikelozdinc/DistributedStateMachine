package tr.edu.itu.bbf.cloudcore.distributed;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
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
import org.springframework.statemachine.persist.StateMachinePersister;
import tr.edu.itu.bbf.cloudcore.distributed.checkpoint.Checkpoint;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
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
    private StateMachinePersister<States, Events, UUID> stateMachinePersister;

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
        Runtime.getRuntime().addShutdownHook(new ExitHook(stateMachine,scanner));
        System.out.println("SMOC is started. From now on, you can send events.");

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
        System.out.println("~~~~~CKPT REPORT~~~~~");
        Map<String, Checkpoint> checkpoints = (Map<String, Checkpoint>) extendedState.getVariables().get("CKPT");
        for(Map.Entry<String, Checkpoint> entry : checkpoints.entrySet()) {
            System.out.println("-----");
            System.out.println("Timestamp -> " + entry.getKey());
            System.out.println("Processed by -> " + entry.getValue().getUuid());
            System.out.println("# CKPTs: -> " + entry.getValue().getNumberOfCKPTs());
            System.out.println("Common Var -> " + entry.getValue().getCommon());
            System.out.println("LocalVarForWaiting -> " + entry.getValue().getLocalVarForWaiting());
            System.out.println("LocalVarForDone -> " + entry.getValue().getLocalVarForDone());
            System.out.println("-----");
        }
    }


    public void sendPayEvent(int timeSleep){
        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                //.setHeader("stateMachineContext", serializeStateMachineContext())
                .build();
        stateMachine.sendEvent(messagePay);
    }
    public void sendReceiveEvent(int timeSleep){
        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                //.setHeader("stateMachineContext", serializeStateMachineContext())
                .build();
        stateMachine.sendEvent(messageReceive);
    }
    public void sendStartFromScratchEvent(int timeSleep){
        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                //.setHeader("stateMachineContext", serializeStateMachineContext())
                .build();
        stateMachine.sendEvent(messageStartFromScratch);
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
                sendPayEvent(timeSleep);
                break;
            case "Receive":
                sendReceiveEvent(timeSleep);
                break;
            case "StartFromScratch":
                sendStartFromScratchEvent(timeSleep);
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

}


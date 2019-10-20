package tr.edu.itu.bbf.cloudcore.distributed;

import org.apache.jute.compiler.JType;
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
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

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
                System.out.print("Event:");
                String event = scanner.next();
                System.out.println("This event will be processed: " + event);
                ProcessEvent(event, timeSleep);
                sleep((long) 5);
                PrintCurrentStatus();
                PerformCheckpoint();
            }
        }catch(IllegalStateException e) {
            System.out.println("Exiting with exception ---> "+ e.toString());
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
        System.out.println("Local variable for Done State: " + extendedState.get("localVarForDone", Integer.class));
        System.out.println("Report before CKPT: ");
        Map<Object, Object> variables = extendedState.getVariables();
        Map<String,Integer> ckpt = (Map<String, Integer>) variables.get("CKPT");
        System.out.println("CKPT.common -> " + ckpt.get("common"));
        System.out.println("CKPT.localVarForWaiting -> " + ckpt.get("localVarForWaiting") );
        System.out.println("CKPT.localVarForDone -> " + ckpt.get("localVarForDone"));
    }


    public void PerformCheckpoint(){
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        ExtendedState  extendedState = context.getExtendedState();
        Map<Object, Object> variables = extendedState.getVariables();
        Map<String,Integer> ckpt = (Map<String, Integer>) variables.get("CKPT");
        System.out.println("---- CKPT IS BEING PERFORMED AS OF NOW -----");
        ckpt.put("common", extendedState.get("common",Integer.class));
        ckpt.put("localVarForWaiting",extendedState.get("localVarForWaiting", Integer.class));
        ckpt.put("localVarForDone",extendedState.get("localVarForDone", Integer.class));
        System.out.println("---STATUS: ");
        System.out.println("common : " + ckpt.get("common"));
        System.out.println("waiting: " + ckpt.get("localVarForWaiting") );
        extendedState.getVariables().put("CKPT", ckpt);
    }

    public void sendPayEvent(int timeSleep){
        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .build();
        stateMachine.sendEvent(messagePay);
    }
    public void sendReceiveEvent(int timeSleep){
        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .build();
        stateMachine.sendEvent(messageReceive);
    }
    public void sendStartFromScratchEvent(int timeSleep){
        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
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
}


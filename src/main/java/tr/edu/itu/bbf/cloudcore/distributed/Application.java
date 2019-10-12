package tr.edu.itu.bbf.cloudcore.distributed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;
import java.io.InputStream;
import java.util.Scanner;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application implements CommandLineRunner {

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

        String type = System.getProperty( "type" );
        System.out.println("TYPE of SMOC is: " + type);


        stateMachine.start();
        //stateMachineEnsemble.join(stateMachine);

        InputStream stream = System.in;
        Scanner scanner = new Scanner(stream);
        System.out.println("SMOC is started. From now on, you can send events.");

        try {
            while (true) {
                System.out.println("Event:");
                String event = scanner.next();
                System.out.println("This event will be sent: " + event);
            }
        }catch(IllegalStateException e) {
            System.out.println("Exiting with exception ---> "+ e.toString());
        }
        scanner.close();
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

        stateMachine.stop();

        System.out.println("*****State machine is stopped.Exiting main program*****");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void sync(String type) throws Exception {
        if(type.equals("sender")){
            System.out.println("Process for sender...");
            //stateMachineEnsemble.setState(new DefaultStateMachineContext<States, Events>(States.DONE,Events.RECEIVE, new HashMap<String, Object>(), new DefaultExtendedState()));
            TimeUnit.SECONDS.sleep(15);
        }
        else if(type.equals("receiver")){
            System.out.println("Process for receiver...");
            StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
            System.out.println("STATE IS " + context.getState().toString());
            System.out.println("EXTENDED STATE IS " + context.getExtendedState().toString());
            System.out.println("EVENT IS " + context.getEvent().toString());
        }

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

}

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
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

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

        if (type.equals("sender")){
            sendPayEvent(timeSleep);
        }
        else if (type.equals("receiver")){
            sendReceiveEvent(timeSleep);
        }


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

}

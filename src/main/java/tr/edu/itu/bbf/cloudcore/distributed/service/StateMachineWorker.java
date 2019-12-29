package tr.edu.itu.bbf.cloudcore.distributed.service;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
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

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

@Service
public class StateMachineWorker {

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @Autowired
    private StateMachineEnsemble<States, Events> stateMachineEnsemble;

    @Autowired
    private ServiceGateway serviceGateway;

    private Integer numberOfEvents;

    static final Logger logger = LoggerFactory.getLogger(StateMachineWorker.class);

    public StateMachineWorker(){
        logger.info("++++++++++++++++++++++++++++++++");
        logger.info("StateMachineWorker::Constructor");
        numberOfEvents = 0;
        logger.info("# of events for this SMOC is initialized to = %d\n",numberOfEvents);
        stateMachine.start();
        stateMachineEnsemble.join(stateMachine);
        logger.info("++++++++++++++++++++++++++++++++");
    }

    public void ProcessEvent(String event, int timeSleep){
        switch(event){
            case "Pay": case "pay": case "PAY":
                numberOfEvents ++;
                sendPayEvent(event, timeSleep);
                break;
            case "Receive": case "receive": case "RECEIVE":
                numberOfEvents ++;
                sendReceiveEvent(event, timeSleep);
                break;
            case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                numberOfEvents ++;
                sendStartFromScratchEvent(event, timeSleep);
                break;
            default:
                System.out.println("Please send one of the events below.");
                System.out.println("Pay/Receive/StartFromScratch");
                break;
        }

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
        this.stateMachine.sendEvent(messagePay);


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

    public  String serializeStateMachineContext(){
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

}

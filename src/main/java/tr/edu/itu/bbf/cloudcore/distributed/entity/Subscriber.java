package tr.edu.itu.bbf.cloudcore.distributed.entity;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Subscriber implements MessageHandler {


    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        Object o_payload = message.getPayload();
        String payload = o_payload.toString();
        System.out.printf("Subscriber endpoint handles the message --> %s\n",payload);
        System.out.println("Message headers are below...");
        /*Get uuid */
        Object O_UUID = message.getHeaders().get("machineId");
        UUID uuid = UUID.fromString(O_UUID.toString());
        System.out.println("UUID -> " + uuid);
        /* Get processed event */
        Object O_event = message.getHeaders().get("processedEvent");
        String processedEvent = O_event.toString();
        System.out.println("Processed Event -> " + processedEvent);
        /* Get source and target states */
        Object O_source = message.getHeaders().get("source");
        String sourceState = O_source.toString();
        System.out.println("Source state -> " + sourceState);
        Object O_target = message.getHeaders().get("target");
        String targetState = O_target.toString();
        System.out.println("Target state -> " + targetState );

    }
}

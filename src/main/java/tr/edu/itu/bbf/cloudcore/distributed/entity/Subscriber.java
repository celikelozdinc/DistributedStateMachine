package tr.edu.itu.bbf.cloudcore.distributed.entity;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class Subscriber implements MessageHandler {


    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        Object o_payload = message.getPayload();
        String payload = o_payload.toString();
        System.out.printf("Subscriber endpoint handles the message --> %s",payload);
    }
}

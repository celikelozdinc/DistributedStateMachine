package tr.edu.itu.bbf.cloudcore.distributed.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class __Publisher {

    private DirectChannel channel;

    @Value("#{checkpointChannel}")
    public void setChannel(DirectChannel channel) { this.channel = channel;}

    public void sendCheckpointInformation(UUID uuid, String sourceState, String processedEvent, String targetState, String smContext){
        System.out.println("__Publisher endpoint releases the message...");
        System.out.println("smContext is below...");
        System.out.println(smContext);
        MessageBuilder<String> payload = MessageBuilder.withPayload("PAYLOAD");
        payload.setHeader("machineId", uuid);
        payload.setHeader("source", sourceState);
        payload.setHeader("processedEvent", processedEvent);
        payload.setHeader("target", targetState);
        payload.setHeader("context", smContext);
        Message<String> message = payload.build();
        channel.send(message);
    }


}

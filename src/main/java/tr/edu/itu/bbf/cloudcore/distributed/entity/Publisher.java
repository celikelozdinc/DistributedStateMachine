package tr.edu.itu.bbf.cloudcore.distributed.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class Publisher {

    private DirectChannel channel;

    @Value("#{checkpointChannel}")
    public void setChannel(DirectChannel channel) { this.channel = channel;}

    public void sendCheckpointInformation(String ckpt){
        System.out.println("Publisher endpoint releases the message...");
        Message<String> message = MessageBuilder
                .withPayload("PAYLOAD")
                .setHeader("CKPT", ckpt)
                .build();
        channel.send(message);
    }

}

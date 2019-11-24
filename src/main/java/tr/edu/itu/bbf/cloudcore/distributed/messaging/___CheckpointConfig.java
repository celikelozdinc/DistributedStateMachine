package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class ___CheckpointConfig {

    @Bean("ckptChannel")
    public MessageChannel channel() {
        return new DirectChannel();
    }

    @Bean
    public ___ICheckpoint ckptService() {
        return new __Checkpoint();
    }

    @Bean
    @ServiceActivator(inputChannel = "ckptChannel")
    public MessageHandler serviceActivator() {
        return new ServiceActivatingHandler(ckptService(), "persist");
    }
}

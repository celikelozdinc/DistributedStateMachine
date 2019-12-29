package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${QUEUE}")
    private String IPC_QUEUE;

    @Value("${EXCHANGE}")
    private String IPC_EXCHANGE;

    @Value("${EVENT_QUEUE_SMOC2}")
    private String EVENT_QUEUE;

    @Value("${EVENT_EXCHANGE_SMOC2}")
    private String EVENT_EXCHANGE;


    @Bean
    Queue ipcQueue() {
        return new Queue(IPC_QUEUE, false);
    }

    @Bean
    DirectExchange ipcExchange() {
        return new DirectExchange(IPC_EXCHANGE);
    }

    @Bean
    Binding binding(Queue ipcQueue, DirectExchange ipcExchange) {
        return BindingBuilder.bind(ipcQueue).to(ipcExchange).with("rpc");
    }

    @Bean
    Queue eventQueue(){ return new Queue(EVENT_QUEUE, false);}

    @Bean
    DirectExchange eventExchange(){return new DirectExchange(EVENT_EXCHANGE);}

    @Bean
    Binding bindingForEvent(Queue eventQueue, DirectExchange eventExchange){
        return BindingBuilder.bind(eventQueue).to(eventExchange).with("rpc");
    }

}

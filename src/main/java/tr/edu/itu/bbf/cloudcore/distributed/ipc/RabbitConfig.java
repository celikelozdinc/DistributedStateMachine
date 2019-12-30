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
    private String EVENT_QUEUE_SMOC2;

    @Value("${EVENT_EXCHANGE_SMOC2}")
    private String EVENT_EXCHANGE_SMOC2;

    @Value("${EVENT_QUEUE_SMOC1}")
    private String EVENT_QUEUE_SMOC1;

    @Value("${EVENT_EXCHANGE_SMOC1}")
    private String EVENT_EXCHANGE_SMOC1;


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
    Queue eventQueue(){ return new Queue(EVENT_QUEUE_SMOC2, false);}

    @Bean
    DirectExchange eventExchange(){return new DirectExchange(EVENT_EXCHANGE_SMOC2);}

    @Bean
    Binding bindingForEvent(Queue eventQueue, DirectExchange eventExchange){
        return BindingBuilder.bind(eventQueue).to(eventExchange).with("rpc");
    }

    @Bean
    Queue eventQueue_smoc1(){ return new Queue(EVENT_QUEUE_SMOC1, false);}

    @Bean
    DirectExchange eventExchange_smoc1(){return new DirectExchange(EVENT_EXCHANGE_SMOC1);}

    @Bean
    Binding bindingForEvent_smoc1(Queue eventQueue_smoc1, DirectExchange eventExchange_smoc1){
        return BindingBuilder.bind(eventQueue_smoc1).to(eventExchange_smoc1).with("rpc");
    }

}

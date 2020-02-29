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

    @Value("${EVENT_QUEUE}")
    private String EVENT_QUEUE;

    @Value("${EVENT_EXCHANGE}")
    private String EVENT_EXCHANGE;


    @Value("${LB_EXCHANGE}")
    private String LB_EXCHANGE;


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


    @Bean
    DirectExchange LbExchange() {
        return new DirectExchange(LB_EXCHANGE);
    }


}

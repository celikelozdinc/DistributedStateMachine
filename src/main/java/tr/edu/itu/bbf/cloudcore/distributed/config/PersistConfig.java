package tr.edu.itu.bbf.cloudcore.distributed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.data.mongodb.MongoDbPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import java.util.UUID;

@Configuration
public class PersistConfig {
    @Bean
    public StateMachineRuntimePersister<States, Events, UUID> mongoPersist (
            MongoDbStateMachineRepository mongoRepository) {
        return new MongoDbPersistingStateMachineInterceptor<States, Events, UUID>(mongoRepository);
    }


    @Bean
    public StateMachinePersister<States, Events, UUID> persister (
            StateMachinePersist<States, Events, UUID> defaultPersist) {
        return new DefaultStateMachinePersister<>(defaultPersist);
    }
}

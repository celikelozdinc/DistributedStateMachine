package tr.edu.itu.bbf.cloudcore.distributed.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import tr.edu.itu.bbf.cloudcore.distributed.entity.Events;
import tr.edu.itu.bbf.cloudcore.distributed.entity.States;

import javax.annotation.PostConstruct;

@Configuration
@EnableStateMachineFactory(name = "factory2")
public class StateMachineConfig_WithoutZK extends EnumStateMachineConfigurerAdapter<States, Events> {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /* Default constructor */
    public void StateMachineConfig_WithoutZK(){
        logger.info(" +++++ CONSTRUCTOR FOR FACTORY +++++");
    }

    @PostConstruct
    public void init() {
        logger.info(" +++++ POSTCONSTRUCTOR FOR FACTORY +++++");
    }



}

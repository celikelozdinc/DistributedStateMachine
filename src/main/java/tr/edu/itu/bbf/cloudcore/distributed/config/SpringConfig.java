package tr.edu.itu.bbf.cloudcore.distributed.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;

import java.util.List;
import java.util.Optional;

@Configuration
public class SpringConfig {

    @Bean
    public ServiceGateway sGateway() {
        return new ServiceGateway() {
            @Override
            public void setCheckpoint(String name) { }

            @Override
            public String getCheckpoint(String name) { return null; }

            @Override
            public ServiceGateway prepareEnvironment() {
                ApplicationContext context = new ClassPathXmlApplicationContext("channel-config.xml");
                ServiceGateway serviceGateway = (ServiceGateway) context.getBean("serviceGateway");
                return serviceGateway;
            }
        };
    }

    @Bean
    public CheckpointDbObjectHandler doHandler(){return new CheckpointDbObjectHandler();}

}

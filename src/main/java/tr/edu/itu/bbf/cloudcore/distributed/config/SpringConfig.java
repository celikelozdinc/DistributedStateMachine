package tr.edu.itu.bbf.cloudcore.distributed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public ServiceGateway serviceGateway() {
        return new ServiceGateway() {
            @Override
            public void setCheckpoint(String name) { }

            @Override
            public String getCheckpoint(String name) { return null; }
        };
    }

}

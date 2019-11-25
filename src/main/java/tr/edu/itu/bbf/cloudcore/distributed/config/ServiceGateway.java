package tr.edu.itu.bbf.cloudcore.distributed.config;

import org.springframework.integration.annotation.Gateway;
import org.springframework.stereotype.Component;

@Component
public interface ServiceGateway {

    @Gateway(requestChannel = "set.channel")
    void setCheckpoint(String name);

    @Gateway(requestChannel = "get.channel")
    String getCheckpoint(String name);

    ServiceGateway prepareEnvironment();

}

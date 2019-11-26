package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface ServiceGateway {

    @Gateway(requestChannel = "set.channel")
    void setCheckpoint(UUID uuid, String context);

    @Gateway(requestChannel = "get.channel")
    String getCheckpoint(String name);


}

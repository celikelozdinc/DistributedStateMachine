package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;

import java.util.List;

@Component
public interface ServiceGateway {

    @Gateway(requestChannel = "set.channel")
    void setCheckpoint(Message<String> ckptMessage);

    @Gateway(requestChannel = "get.channel")
    List<CheckpointDbObject> getCheckpoint(Message<String> getMessage);


}

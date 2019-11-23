package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(name = "ckptGateway", defaultRequestChannel = "ckptChannel")
public interface ICheckpoint {
    void persist(String context);
}

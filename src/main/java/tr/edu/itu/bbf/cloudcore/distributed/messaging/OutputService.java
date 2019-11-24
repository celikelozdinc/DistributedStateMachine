package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

@Component
public class OutputService {

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    @ServiceActivator
    public void printValue(String value){
        System.out.printf("Context is .. %s", value);
    }
}

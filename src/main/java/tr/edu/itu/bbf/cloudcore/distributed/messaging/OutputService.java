package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;

@Service
public class OutputService {

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    @ServiceActivator
    public void printValue(String value){
        System.out.printf("Context is .. %s\n", value);
        CheckpointDbObject dbObject = new CheckpointDbObject(1, value);
        dbObjectHandler.insertCheckpoint(dbObject);
    }
}

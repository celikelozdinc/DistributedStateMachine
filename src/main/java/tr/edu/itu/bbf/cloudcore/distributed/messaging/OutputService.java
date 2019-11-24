package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;

@Component
public class OutputService {
    @Autowired
    private CheckpointRepository checkpointRepository;

    @ServiceActivator
    public void printValue(String value){
        System.out.printf("Context is .. %s\n", value);
        CheckpointDbObjectHandler dbObjectHandler = new CheckpointDbObjectHandler(checkpointRepository);
        CheckpointDbObject dbObject = new CheckpointDbObject(1, value);
        dbObjectHandler.insertCheckpoint(dbObject);
    }
}

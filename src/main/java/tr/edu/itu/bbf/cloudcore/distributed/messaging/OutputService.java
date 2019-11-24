package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;

@Component
@EnableMongoRepositories(basePackageClasses= CheckpointRepository.class)
public class OutputService {

    @ServiceActivator
    public void printValue(String value){
        System.out.printf("Context is .. %s\n", value);
        CheckpointDbObjectHandler dbObjectHandler = new CheckpointDbObjectHandler();
        CheckpointDbObject dbObject = new CheckpointDbObject("timestamp", value);
        dbObjectHandler.insertCheckpoint(dbObject);
    }
}

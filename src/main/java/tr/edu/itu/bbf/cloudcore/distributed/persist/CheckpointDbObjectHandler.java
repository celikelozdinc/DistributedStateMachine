package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckpointDbObjectHandler {

    @Autowired
    private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(CheckpointDbObject checkpointDbObject){
        System.out.println("INSERT");
        return checkpointRepository.insert(checkpointDbObject);
    }

    // UPDATE
    public CheckpointDbObject updateCheckpoint(CheckpointDbObject checkpointDbObject) {
        System.out.println("UPDATE");
        return checkpointRepository.save(checkpointDbObject);
    }

}

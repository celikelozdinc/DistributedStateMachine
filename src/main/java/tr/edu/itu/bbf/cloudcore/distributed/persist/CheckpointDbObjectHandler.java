package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckpointDbObjectHandler {

    @Autowired
    private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(CheckpointDbObject checkpointDbObject){
        return checkpointRepository.insert(checkpointDbObject);
    }

    // UPDATE
    public CheckpointDbObject updateCheckpoint(CheckpointDbObject checkpointDbObject) {
        return checkpointRepository.save(checkpointDbObject);
    }

}

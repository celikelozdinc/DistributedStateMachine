package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;


@Component
public class CheckpointDbObjectHandler {

    @Autowired
    private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(@NotNull CheckpointDbObject checkpointDbObject){
        System.out.println("INSERT CHECKPOINT");
        System.out.println("Timestamp inside db object ->" + checkpointDbObject.timestamp.toString());
        try {
            return checkpointRepository.insert(checkpointDbObject);
        } catch(Exception ex) {
            System.out.println("Can not insert :(");
            System.out.println("Exception...");
            System.out.println(ex.toString());
            return checkpointDbObject;
        }

    }

    // UPDATE
    public CheckpointDbObject updateCheckpoint(CheckpointDbObject checkpointDbObject) {
        System.out.println("UPDATE");
        return checkpointRepository.save(checkpointDbObject);
    }

}

package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;



@Component
public class CheckpointDbObjectHandler {

    @Autowired
    private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(@NotNull CheckpointDbObject checkpointDbObject){
        System.out.println("INSERT CHECKPOINT");
        System.out.println("Timestamp inside db object ->" + checkpointDbObject.timestamp.toString());
        if (checkpointRepository == null) {System.out.println("Checkpoint repository is null.");}
        if(checkpointDbObject == null){System.out.println("Checkpoint db object is null.");}
        try {
            return checkpointRepository.insert(checkpointDbObject);
        } catch(Exception ex) {
            System.out.println("Can not insert :(");
            System.out.println("Exception...");
            ex.printStackTrace();
            return checkpointDbObject;
        }

    }

    // UPDATE
    public CheckpointDbObject updateCheckpoint(CheckpointDbObject checkpointDbObject) {
        System.out.println("UPDATE");
        return checkpointRepository.save(checkpointDbObject);
    }

}

package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;

import java.util.List;
import java.util.UUID;


@Component
public class CheckpointDbObjectHandler {

    @Autowired
    private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(@NotNull CheckpointDbObject checkpointDbObject){
        System.out.println("DBOBJECTHANDLER::INSERT CHECKPOINT");
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
        System.out.println("UPDATE CHECKPOINT");
        return checkpointRepository.save(checkpointDbObject);
    }

    //GET
    public List<CheckpointDbObject> getAllCheckpoints(){
        System.out.println("CheckpointDbObjectHandler.getAllCheckpoints():: GET CHECKPOINTS");
        return checkpointRepository.findAll();
        /*
        System.out.println("UUID to be searched in database: " + uuid.toString());
        System.out.println("# matching records in database: " + (checkpointRepository.findBySmocUuidLike(uuid)).size());
        return checkpointRepository.findBySmocUuidLike(uuid);
         */
    }

}

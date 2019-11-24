package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;

import java.util.List;
import java.util.Optional;


@Component
public class CheckpointDbObjectHandler {

    @Autowired
    private CheckpointRepository checkpointRepository;

    // INSERT
    public CheckpointDbObject insertCheckpoint(@NotNull CheckpointDbObject checkpointDbObject){
        System.out.println("INSERT CHECKPOINT");
        System.out.println("Timestamp inside db object ->" + checkpointDbObject.timestamp.toString());
        if (checkpointRepository == null) {System.out.println("Checkpoint repository is null.");}
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
    public void updateCheckpoint(CheckpointDbObject checkpointDbObject) {
        System.out.println("UPDATE");
        //return checkpointRepository.save(checkpointDbObject);
    }

}

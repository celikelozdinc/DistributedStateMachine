package tr.edu.itu.bbf.cloudcore.distributed.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

@Component
public class Processor {

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    public Processor(CheckpointDbObjectHandler dbObjectHandler) {
        this.dbObjectHandler = dbObjectHandler;
    }

    public void processCheckpoint(String context){
        CheckpointDbObject dbObject = new CheckpointDbObject("timestamp", context);
        dbObjectHandler.insertCheckpoint(dbObject);
    }


}

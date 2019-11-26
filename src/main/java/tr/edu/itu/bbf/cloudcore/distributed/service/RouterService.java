package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

import java.util.Calendar;
import java.util.UUID;

@Service
public class RouterService {

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    public void setCheckpoint(String timestamp, String context) {
        CheckpointDbObject dbObject = new CheckpointDbObject(timestamp, context);
        dbObjectHandler.insertCheckpoint(dbObject);
    }

    public String getCheckpoint(){return "N/A";}

}

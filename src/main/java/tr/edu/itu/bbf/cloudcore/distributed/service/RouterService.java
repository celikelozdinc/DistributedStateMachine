package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

@Service
public class RouterService {

    private CheckpointDbObjectHandler dbObjectHandler;

    public void setCheckpoint(String context) {
        System.out.printf("SMOC context inside RouterService : %s\n",context);
        CheckpointDbObject dbObject = new CheckpointDbObject(1, context);
        dbObjectHandler.insertCheckpoint(dbObject);
    }

    public String getCheckpoint(){return "N/A";}
}

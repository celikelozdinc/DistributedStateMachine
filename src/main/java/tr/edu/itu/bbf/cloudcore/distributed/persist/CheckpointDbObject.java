package tr.edu.itu.bbf.cloudcore.distributed.persist;

/** DTO = Data Transfer Object */

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection="Checkpoints")
public class CheckpointDbObject {

    @Id
    public String timestamp;

    @Field("context")
    private String context;

    //@Field("uuid")
    //private UUID uuid;

    /*
    private String sourceState;
    private String processedEvent;
    private String targetState;
     */

    @PersistenceConstructor
    public CheckpointDbObject(String timestamp, String context) {
        this.timestamp = timestamp;
        this.context = context;
    }


/*
    @PersistenceConstructor
    public CheckpointDbObject(String timestamp, UUID uuid, String ss, String pe, String ts, String context) {
        this.timestamp = timestamp;
        this.context = context;
        this.uuid = uuid;
        this.sourceState = ss;
        this.processedEvent = pe;
        this.targetState = ts;
        System.out.printf("CONTEXT IN PERSISTENCE CONSTRUCTOR --->  %s\n", this.smocContext);
        System.out.printf("PARAMETERS: %s %s %s %s\n",timestamp,ss,pe,ts);
    }
    */


}

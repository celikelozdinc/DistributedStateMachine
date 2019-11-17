package tr.edu.itu.bbf.cloudcore.distributed.persist;

/** DTO = Data Transfer Object */

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection="Checkpoints")
public class CheckpointDbObject {

    @Id
    public String timestamp;

    private UUID uuid;
    private String sourceState;
    private String processedEvent;
    private String targetState;
    private String context;

    /*
    @PersistenceConstructor
    public CheckpointDbObject(String timestamp, UUID uuid, String ss, String pe, String ts) {
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.sourceState = ss;
        this.processedEvent = pe;
        this.targetState = ts;
    }
     */

    @PersistenceConstructor
    public CheckpointDbObject(String timestamp, String context) {
        this.timestamp = timestamp;
        this.context = context;
    }



}

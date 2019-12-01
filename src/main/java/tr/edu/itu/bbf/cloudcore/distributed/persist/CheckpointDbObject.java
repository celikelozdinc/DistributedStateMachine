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
    @Field("uuid")
    private UUID uuid;
    @Field("event")
    private String processedEvent;
    @Field("source")
    private String sourceState;
    @Field("target")
    private String targetState;


    @PersistenceConstructor
    public CheckpointDbObject(String timestamp, UUID uuid, String sourceState, String processedEvent, String targetState, String context) {
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.sourceState = sourceState;
        this.processedEvent=processedEvent;
        this.targetState = targetState ;
        this.context = context;
    }

    public String getProcessedEvent(){return this.processedEvent;}
    public UUID getUuid(){return this.uuid;}

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

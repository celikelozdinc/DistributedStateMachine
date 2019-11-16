package tr.edu.itu.bbf.cloudcore.distributed.persist;

/** DTO = Data Transfer Object */

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection="Checkpoints")
public class CheckpointDbObject {

    @Id
    public UUID id;

    private String processedEvent;

    @PersistenceConstructor
    public CheckpointDbObject(UUID uuid, String pe) {
        this.processedEvent = pe;
        this.id = uuid;
    }

}

package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.data.mongodb.repository.MongoRepository;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckpointRepository extends MongoRepository<CheckpointDbObject, String> {

    List<CheckpointDbObject> findByProcessedEventLike(String event);
    List<CheckpointDbObject> findBySmocUuidLike(UUID uuid);

}
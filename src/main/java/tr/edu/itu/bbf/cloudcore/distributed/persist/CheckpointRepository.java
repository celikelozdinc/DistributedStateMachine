package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.data.mongodb.repository.MongoRepository;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckpointRepository extends MongoRepository<CheckpointDbObject, String> {

}
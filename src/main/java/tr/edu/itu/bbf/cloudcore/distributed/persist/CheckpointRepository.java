package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;

@Repository
public interface CheckpointRepository extends MongoRepository<CheckpointDbObject, Integer> {

}
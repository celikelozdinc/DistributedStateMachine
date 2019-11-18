package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckpointRepository extends MongoRepository<CheckpointDbObject, Integer> {

}
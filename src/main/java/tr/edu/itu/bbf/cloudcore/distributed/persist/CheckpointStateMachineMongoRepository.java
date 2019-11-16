package tr.edu.itu.bbf.cloudcore.distributed.persist;

import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckpointStateMachineMongoRepository  extends MongoDbStateMachineRepository {

}

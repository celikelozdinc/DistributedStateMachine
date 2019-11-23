package tr.edu.itu.bbf.cloudcore.distributed.messaging;
import tr.edu.itu.bbf.cloudcore.distributed.messaging.ICheckpoint;

public class Checkpoint implements ICheckpoint {

    @Override
    public void persist(String context) {
        System.out.printf("Context is : %s",context);
    }
}

package tr.edu.itu.bbf.cloudcore.distributed.messaging;

public class __Checkpoint implements ___ICheckpoint {

    @Override
    public void persist(String context) {
        System.out.printf("Context is : %s",context);
    }
}

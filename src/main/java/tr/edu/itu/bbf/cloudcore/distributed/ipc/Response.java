package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import java.io.Serializable;
import java.util.UUID;

public class Response implements Serializable {

    private String sourceState;
    private String processedEvent;
    private String targetState;

    public Response(){}

    public Response(String ss,String pe,String ds){
        this.sourceState = ss;
        this.processedEvent = pe;
        this.targetState = ds;
    }

    public String getDestinationState() {
        return targetState;
    }

    public String getProcessedEvent() {
        return processedEvent;
    }

    public String getSourceState() {
        return sourceState;
    }

    public void setDestinationState(String destinationState) {
        this.targetState = destinationState;
    }

    public void setProcessedEvent(String processedEvent) {
        this.processedEvent = processedEvent;
    }

    public void setSourceState(String sourceState) {
        this.sourceState = sourceState;
    }


}

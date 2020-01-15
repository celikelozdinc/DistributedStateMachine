package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import java.io.Serializable;

public class Response implements Serializable {

    private String sourceState;
    private String processedEvent;
    private String targetState;
    private Integer eventNumber;

    public Response(){}

    public Response(Integer eventNumber,String ss,String pe,String ds){
        this.eventNumber = eventNumber;
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

    public void setEventNumber(Integer eventNumber) {
        this.eventNumber = eventNumber;
    }

    public Integer getEventNumber() {
        return eventNumber;
    }
}

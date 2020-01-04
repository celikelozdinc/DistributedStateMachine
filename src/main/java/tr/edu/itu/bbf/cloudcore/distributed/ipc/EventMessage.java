package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import java.io.Serializable;

public class EventMessage implements Serializable {

    public String event;
    public Integer eventNumber;
    public String sender;

    public EventMessage(){}

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSender() { return sender; }

    public void setSender(String sender) { this.sender = sender; }

    public Integer getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(Integer eventNumber) {
        this.eventNumber = eventNumber;
    }
}

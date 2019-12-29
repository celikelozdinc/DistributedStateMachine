package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import java.io.Serializable;

public class EventMessage implements Serializable {

    public String event;

    public EventMessage(){}

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}

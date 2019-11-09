package tr.edu.itu.bbf.cloudcore.distributed.checkpoint;

import java.util.UUID;

public class Checkpoint {

    private Integer common;
    private Integer localVarForWaiting;
    private Integer localVarForDone;
    private UUID uuid;


    /* Empty Constructor */
    public Checkpoint(){}

    public Integer getCommon() {
        return common;
    }
    public void setCommon(Integer value) {
        this.common = value;
    }

    public Integer getLocalVarForDone() {
        return localVarForDone;
    }
    public void setLocalVarForDone(Integer localVarForDone) {
        this.localVarForDone = localVarForDone;
    }

    public Integer getLocalVarForWaiting() {
        return localVarForWaiting;
    }
    public void setLocalVarForWaiting(Integer localVarForWaiting) {
        this.localVarForWaiting = localVarForWaiting;
    }

    public UUID getUuid() {return uuid;}
    public void setUuid(UUID uuid) {this.uuid = uuid; }
}

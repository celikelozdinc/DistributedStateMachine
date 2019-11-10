package tr.edu.itu.bbf.cloudcore.distributed.checkpoint;

import java.util.UUID;

public class Checkpoint {

    private Integer common;
    private Integer localVarForWaiting;
    private Integer localVarForDone;
    private UUID uuid;
    private Integer numberOfCKPTs;


    /* Empty Constructor */
    public Checkpoint(){ numberOfCKPTs = 0;}

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

    public Integer getNumberOfCKPTs() {
        return numberOfCKPTs;
    }
    public void setNumberOfCKPTs(Integer numberOfCKPTs) { this.numberOfCKPTs = numberOfCKPTs; }
    public void increaseNumberOfCKPTs(){ numberOfCKPTs ++; }
}

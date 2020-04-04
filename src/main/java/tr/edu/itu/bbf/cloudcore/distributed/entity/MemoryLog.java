package tr.edu.itu.bbf.cloudcore.distributed.entity;

public class MemoryLog {
    private Integer VmSize, VmPeak, VmHWM, VmRSS, VmData;

    public MemoryLog(){
        /* Initialize footprint */
        VmSize = VmPeak = VmHWM = VmRSS = VmData =  -1;
    }

    public MemoryLog(Integer size, Integer peak, Integer hwm, Integer rss, Integer data ){
        /* Initialize footprint with values */
        this.VmSize = size;
        this.VmPeak = peak;
        this.VmHWM = hwm;
        this.VmRSS = rss;
        this.VmData = data;
    }

    public Integer getVmData() {
        return VmData;
    }

    public Integer getVmHWM() {
        return VmHWM;
    }

    public Integer getVmPeak() {
        return VmPeak;
    }

    public Integer getVmRSS() {
        return VmRSS;
    }

    public Integer getVmSize() {
        return VmSize;
    }
}

package tr.edu.itu.bbf.cloudcore.distributed.entity;

public class MemoryLogger {

    private String VmSize, VmPeak, VmHWM, VmRSS, VmData;

    public MemoryLogger(){
        /* Initialize footprint */
        VmSize = VmPeak = VmHWM = VmRSS = VmData = "";
    }

    public MemoryLogger(String size, String peak, String hwm, String rss, String data ){
        /* Initialize footprint with values */
        this.VmSize = size;
        this.VmPeak = peak;
        this.VmHWM = hwm;
        this.VmRSS = rss;
        this.VmData = data;
    }

    public void storeMemoryFootprint(String size, String peak, String hwm, String rss, String data ){
        this.VmSize = size;
        this.VmPeak = peak;
        this.VmHWM = hwm;
        this.VmRSS = rss;
        this.VmData = data;
    }
}

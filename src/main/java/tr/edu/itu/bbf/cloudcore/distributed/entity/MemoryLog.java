package tr.edu.itu.bbf.cloudcore.distributed.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MemoryLog {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ArrayList<Integer> memoryLogList;
    private Integer max;
    private Integer min;
    private Integer delta;

    public MemoryLog(){
        /* Initialize footprint */
        memoryLogList = new ArrayList<>();
        /* Initialize max and min values */
        max = Integer.MIN_VALUE;
        min = Integer.MAX_VALUE;
    }

    public void store(Integer footprint){
        /* Add current footprint */
        memoryLogList.add(footprint);
        /* Update min, max and delta */
        if(footprint > max){
            logger.info("Updating MAX from {} to {}",max,footprint);
            max = footprint;
        }
        if(footprint < min){
            logger.info("Updating MIN from {} to {}",min,footprint);
            min = footprint;
        }

        delta = max - min;
    }

    public Integer sizeOfMemoryLog(){ return this.memoryLogList.size(); }

    public Integer getMax() { return max; }

    public Integer getMin() { return min; }

    public Integer getDelta() { return delta; }

    public ArrayList<Integer> getMemoryLogList() { return memoryLogList; }
}

package tr.edu.itu.bbf.cloudcore.distributed.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MemoryLog {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ArrayList<Integer> memoryLogList;
    private Integer max;
    private Integer min;

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
        /* Update min and max */
        if(footprint > max){
            logger.info("Updating MAX from {} to {}",max,footprint);
            max = footprint;
        }
        if(footprint < min){
            logger.info("Updating MIN from {} to {}",min,footprint);
            min = footprint;
        }
    }

    public Integer sizeOfMemoryLog(){ return this.memoryLogList.size(); }

    public Integer getMax() { return max; }

    public Integer getMin() { return min; }

    public ArrayList<Integer> getMemoryLogList() { return memoryLogList; }
}

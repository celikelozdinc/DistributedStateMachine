package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.entity.MemoryLog;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class MemoryLogger {
    private ArrayList<MemoryLog> memoryLogList;

    private Integer sum_VmPeak, sum_VmSize,sum_VmHWM, sum_VmRss,sum_VmData ;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MemoryLogger(){}

    @PostConstruct
    public void init() {
        logger.info("+++++MemoryLogger::PostConstructor+++++");
        /*Initialize list for storing each memory footprint calculation*/
        memoryLogList = new ArrayList<MemoryLog>();
        /* Sum of each metric */
        this.sum_VmPeak = 0; this.sum_VmSize=0; this. sum_VmHWM= 0 ; this.sum_VmRss= 0 ; this.sum_VmData = 0 ;

    }

    public void storeMemoryLog(String peak, String size, String hwm, String rss, String data){
        /* Cast to Integer & store it*/
        memoryLogList.add(new MemoryLog(Integer.parseInt(peak),Integer.parseInt(size),Integer.parseInt(hwm),Integer.parseInt(rss), Integer.parseInt(data)));
        /* Statistics about memoryfootprint */
        this.sum_VmPeak =  this.sum_VmPeak + Integer.parseInt(peak);
        this.sum_VmSize = this.sum_VmSize + Integer.parseInt(size);
        this.sum_VmHWM = this. sum_VmHWM + Integer.parseInt(hwm);
        this.sum_VmRss =  this.sum_VmRss + Integer.parseInt(rss);
        this.sum_VmData = this.sum_VmData + Integer.parseInt(data);
        logger.info("Sum of each memoryfootprint metric: {}, {}, {}, {}, {}", this.sum_VmPeak,this.sum_VmSize,this.sum_VmHWM,this.sum_VmRss,this.sum_VmData);
    }

    public Integer sizeOfMemoryLog(){ return memoryLogList.size(); }



}

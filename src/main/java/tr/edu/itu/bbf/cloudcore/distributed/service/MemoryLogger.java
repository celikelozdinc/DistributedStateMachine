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
        logger.info("+++++MemoryLogger::storeMemoryLog+++++");
        /* Cast to Integer & store it*/
        Integer VmPeak = Integer.parseInt(peak);
        Integer VmSize = Integer.parseInt(size);
        Integer VmHWM = Integer.parseInt(hwm);
        Integer VmRSS = Integer.parseInt(rss);
        Integer VmData = Integer.parseInt(data);
        memoryLogList.add(new MemoryLog(VmPeak,VmSize,VmHWM,VmRSS, VmData));
        /* Statistics about memoryfootprint */
        this.sum_VmPeak =  this.sum_VmPeak + VmPeak;
        double avg_VmPeak = ((double) this.sum_VmPeak) / (this.memoryLogList.size());
        this.sum_VmSize = this.sum_VmSize + VmSize;
        double avg_VmSize = ((double) this.sum_VmSize) /  (this.memoryLogList.size());
        this.sum_VmHWM = this. sum_VmHWM + VmHWM;
        double avg_VmHWM = ((double) this.sum_VmHWM) /  (this.memoryLogList.size());
        this.sum_VmRss =  this.sum_VmRss + VmRSS;
        double avg_VmRSS = ((double) this.sum_VmRss) / (this.memoryLogList.size());
        this.sum_VmData = this.sum_VmData + VmData;
        double avg_VmData = ((double) this.sum_VmData) / (this.memoryLogList.size());
        logger.info("Current of each memory footprint metric >  {}, {}, {}, {}, {}",VmPeak,VmSize,VmHWM,VmRSS,VmData);
        logger.info("Sum of each memory footprint metric >  {}, {}, {}, {}, {}", this.sum_VmPeak,this.sum_VmSize,this.sum_VmHWM,this.sum_VmRss,this.sum_VmData);
        logger.info("Avg of each memory footprint metric >  {}, {}, {}, {}, {}", avg_VmPeak, avg_VmSize,avg_VmHWM,avg_VmRSS,avg_VmData);
    }

    public Integer sizeOfMemoryLog(){ return this.memoryLogList.size(); }



}
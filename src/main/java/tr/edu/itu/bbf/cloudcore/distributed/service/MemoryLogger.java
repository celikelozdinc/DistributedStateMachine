package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.entity.MemoryLog;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class MemoryLogger {
    private MemoryLog VmPeak,VmSize,VmHWM,VmRSS,VmData;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MemoryLogger(){}

    @PostConstruct
    public void init() {
        logger.info("+++++MemoryLogger::PostConstructor+++++");
        /*Initialize Log object for storing each memory footprint metric */
        VmPeak = new MemoryLog();
        VmSize = new MemoryLog();
        VmHWM = new MemoryLog();
        VmRSS = new MemoryLog();
        VmData = new MemoryLog();
    }

    public void storeMemoryLog(String peak, String size, String hwm, String rss, String data){
        logger.info("+++++MemoryLogger::storeMemoryLog+++++");
        /* Cast to Integer & store it*/
        Integer VmPeak_measure = Integer.parseInt(peak); this.VmPeak.store(VmPeak_measure);
        Integer VmSize_measure = Integer.parseInt(size); this.VmSize.store(VmSize_measure);
        Integer VmHWM_measure = Integer.parseInt(hwm); this.VmHWM.store(VmHWM_measure);
        Integer VmRSS_measure = Integer.parseInt(rss);this.VmRSS.store(VmRSS_measure);
        Integer VmData_measure = Integer.parseInt(data); this.VmData.store(VmData_measure);
        //logger.info("Current of each memory footprint metric >  {}, {}, {}, {}, {}",VmPeak,VmSize,VmHWM,VmRSS,VmData);
        //logger.info("Sum of each memory footprint metric >  {}, {}, {}, {}, {}", this.sum_VmPeak,this.sum_VmSize,this.sum_VmHWM,this.sum_VmRss,this.sum_VmData);
        //logger.info("Avg of each memory footprint metric >  {}, {}, {}, {}, {}", avg_VmPeak, avg_VmSize,avg_VmHWM,avg_VmRSS,avg_VmData);
        logger.info("Size of VmPeak memoryfootprint array --> {}",VmPeak.sizeOfMemoryLog());
        logger.info("Size of VmSize memoryfootprint array --> {}",VmSize.sizeOfMemoryLog());

        logger.info("Size of VmHWM memoryfootprint array --> {}",VmHWM.sizeOfMemoryLog());
        logger.info("Max of VmHWM memoryfootprint array --> {}",VmHWM.getMax());
        logger.info("Min of VmHWM memoryfootprint array --> {}",VmHWM.getMin());
        logger.info("VmHWM memoryfootprint array --> {}",VmHWM.getMemoryLogList());

        logger.info("Size of VmRSS memoryfootprint array --> {}",VmRSS.sizeOfMemoryLog());
        logger.info("Max of VmRSS memoryfootprint array --> {}",VmRSS.getMax());
        logger.info("Min of VmRSS memoryfootprint array --> {}",VmRSS.getMin());
        logger.info("VmRSS memoryfootprint array --> {}",VmRSS.getMemoryLogList());

        logger.info("Size of VmData memoryfootprint array --> {}",VmData.sizeOfMemoryLog());
        logger.info("Max of VmData memoryfootprint array --> {}",VmData.getMax());
        logger.info("Min of VmData memoryfootprint array --> {}",VmData.getMin());
        logger.info("VmData memoryfootprint array --> {}",VmData.getMemoryLogList());
    }


}

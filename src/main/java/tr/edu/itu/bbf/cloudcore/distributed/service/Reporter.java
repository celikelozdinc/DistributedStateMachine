package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.*;

@Service
public class Reporter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String PID;

    public Reporter(){
        logger.info("+++++Reporter::Constructor+++++");
    }

    private String baseCommandForMemoryMetrics;

    @Autowired
    private MemoryLogger memoryLogger;

    private String VmSize_t0, VmPeak_t0, VmHWM_t0, VmRSS_t0, VmData_t0;

    private String VmSize_current, VmPeak_current, VmHWM_current, VmRSS_current, VmData_current;

    @PostConstruct
    public void init() {
        logger.info("+++++Reporter::PostConstructor+++++");
        /* Get PID */
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        /* result: <PID>@<hostname>*/
        int p = vmName.indexOf("@");
        this.PID = vmName.substring(0, p);
        logger.info("PID of JVM is --> {}",this.PID);

        //$(grep "VmSize" /proc/"$current_pid"/status | awk -F 'VmSize:|kB' '{print $2}' | xargs)
        baseCommandForMemoryMetrics = "grep %s /proc/%s/status|awk -F '%s:|kB' '{print $2}' | xargs";

    }

    public String prepareCommand(String metric){
        String formattedCommand = String.format(this.baseCommandForMemoryMetrics,metric,this.PID,metric);
        return formattedCommand;
    }

    public String runCommand(String command) throws IOException {
        //Prepare process
        Process process =
                new ProcessBuilder(new String[] {"bash", "-c", command})
                        .redirectErrorStream(true)
                        .directory(new File("."))
                        .start();

        ArrayList<String> output = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ( (line = br.readLine()) != null )
            output.add(line);

        // OUTPUT --> [2497892]
        return output.get(0);
    }

    public void calculateInitialMemoryFootprint() throws IOException {
        //VmPeak,VmSize,VmHWM,VmRSS,VmData

        /* VmPeak */
        String VmPeakCommand = prepareCommand("VmPeak");
        this.VmPeak_t0 = runCommand(VmPeakCommand);

        /* VmSize */
        String VmSizeCommand = prepareCommand("VmSize");
        this.VmSize_t0 = runCommand(VmSizeCommand);

        /* VmHWM */
        String VmHWMCommand = prepareCommand("VmHWM");
        this.VmHWM_t0 = runCommand(VmHWMCommand);

        /* VmRSS */
        String VmRSSCommand = prepareCommand("VmRSS");
        this.VmRSS_t0 = runCommand(VmRSSCommand);

        /* VmData */
        String VmDataCommand = prepareCommand("VmData");
        this.VmData_t0 = runCommand(VmDataCommand);

        logger.info("InitialMemoryFootprint > VmPeak,VmSize,VmHWM,VmRSS,VmData > {},{},{},{},{}",this.VmPeak_t0,this.VmSize_t0,this.VmHWM_t0,this.VmRSS_t0,this.VmData_t0);
    }

    public void calculateDeltaMemoryFootprint() throws IOException {
        //VmPeak,VmSize,VmHWM,VmRSS,VmData

        /* VmPeak */
        String VmPeakCommand = prepareCommand("VmPeak");
        this.VmPeak_current = runCommand(VmPeakCommand);
        Integer delta_VmPeak = Integer.parseInt(this.VmPeak_current) - Integer.parseInt(this.VmPeak_t0);

        /* VmSize */
        String VmSizeCommand = prepareCommand("VmSize");
        this.VmSize_current = runCommand(VmSizeCommand);
        Integer delta_VmSize = Integer.parseInt(this.VmSize_current) - Integer.parseInt(this.VmSize_t0);

        /* VmHWM */
        String VmHWMCommand = prepareCommand("VmHWM");
        this.VmHWM_current = runCommand(VmHWMCommand);
        Integer delta_VmHWM = Integer.parseInt(this.VmHWM_current) - Integer.parseInt(this.VmHWM_t0);

        /* VmRSS */
        String VmRSSCommand = prepareCommand("VmRSS");
        this.VmRSS_current = runCommand(VmRSSCommand);
        Integer delta_VmRSS = Integer.parseInt(this.VmRSS_current) - Integer.parseInt(this.VmRSS_t0);

        /* VmData */
        String VmDataCommand = prepareCommand("VmData");
        this.VmData_current = runCommand(VmDataCommand);
        Integer delta_VmData = Integer.parseInt(this.VmData_current) - Integer.parseInt(this.VmData_t0);

        logger.info("CurrentMemoryFootprint > VmPeak,VmSize,VmHWM,VmRSS,VmData > {},{},{},{},{}",this.VmPeak_current,this.VmSize_current,this.VmHWM_current,this.VmRSS_current,this.VmData_current);
        logger.info("DeltaMemoryFootprint > VmPeak,VmSize,VmHWM,VmRSS,VmData > {},{},{},{},{}",delta_VmPeak,delta_VmSize,delta_VmHWM,delta_VmRSS,delta_VmData);

    }

    public void logMemoryFootprint() throws IOException {
        //VmPeak,VmSize,VmHWM,VmRSS,VmData

        /* VmPeak */
        String VmPeakCommand = prepareCommand("VmPeak");
        String VmPeak = runCommand(VmPeakCommand);

        /* VmSize */
        String VmSizeCommand = prepareCommand("VmSize");
        String VmSize = runCommand(VmSizeCommand);

        /* VmSize */
        String VmHWMCommand = prepareCommand("VmHWM");
        String VmHWM = runCommand(VmHWMCommand);

        /* VmRSS */
        String VmRSSCommand = prepareCommand("VmRSS");
        String VmRSS = runCommand(VmRSSCommand);

        /* VmData */
        String VmDataCommand = prepareCommand("VmData");
        String VmData = runCommand(VmDataCommand);

        /* Store */
        memoryLogger.storeMemoryLog(VmPeak,VmSize,VmHWM,VmRSS, VmData);
        logger.info("Size of memoryfootprint array --> {}",memoryLogger.sizeOfMemoryLog());


        //logger.info("DeltaMemoryFootprint > VmPeak,VmSize,VmHWM,VmRSS,VmData > {},{},{},{},{}",delta_VmPeak,delta_VmSize,delta_VmHWM,delta_VmRSS,delta_VmData);
    }
}

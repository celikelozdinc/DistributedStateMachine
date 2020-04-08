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
    }
}

package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private String VmSize_t0, VmPeak_t0;

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
        /* VmSize */
        String VmSizeCommand = prepareCommand("VmSize");
        logger.info("Command for VmSize: {}",VmSizeCommand);
        this.VmSize_t0 = runCommand(VmSizeCommand);
        logger.info("VmSize_t0 = {}",this.VmSize_t0);

        /* VmPeak */
        String VmPeakCommand = prepareCommand("VmPeak");
        logger.info("Command for VmPeak: {}",VmPeakCommand);
        this.VmPeak_t0 = runCommand(VmPeakCommand);
        logger.info("VmPeak_t0 = {}",this.VmPeak_t0);


    }
}

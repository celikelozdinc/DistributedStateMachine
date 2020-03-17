package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@Service
public class Reporter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String PID;

    public Reporter(){
        logger.info("+++++Reporter::Constructor+++++");
    }

    private String baseCommandForMemoryMetrics;

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

    public void calculateInitialMemoryFootprint() throws IOException, InterruptedException {
        /* Prepare command */
        String VmSizeCommand = prepareCommand("VmSize");
        logger.info("Command for VmSize: {}",VmSizeCommand);

        ProcessBuilder builder = new ProcessBuilder(VmSizeCommand);
        Process p = builder.start();
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }
        int exitVal = p.waitFor();
        if (exitVal == 0) {
            logger.info("Success!");
            logger.info("output --> {}",output);
            //System.exit(0);
        } else {
            //abnormal...
        }


        //String VmSize_t0 = stdInput.readLine();
        //logger.info("VmSize_t0 = {}",VmSize_t0);
    }
}

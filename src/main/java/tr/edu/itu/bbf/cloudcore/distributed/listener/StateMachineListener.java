package tr.edu.itu.bbf.cloudcore.distributed.listener;

import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;

@Component
@WithStateMachine(name = "DistributedStateMachine")
public class StateMachineListener {
    private File logfile;
    private File jarfile = new File(System.getProperty("java.class.path"));

    /** Default Constructor **/
    public StateMachineListener(){createLogFile();}

    public String getTimeStamp(){
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int ms = now.get(Calendar.MILLISECOND);

        String ts = year + "." + month + "." +  day + "_" + hour + "." + minute + "." + second + "." + ms;
        return ts;
    }

    public void createLogFile(){

        File dir = jarfile.getAbsoluteFile().getParentFile();
        String path = dir.toString();
        System.out.println("PATH ---> " + path);


        String fd = path + "/log_" + getTimeStamp() + ".txt" ;
        try
        {
            logfile = new File(fd);
            if (!logfile.exists()) {
                System.out.println("Logfile does not exist. Create new one.");
                logfile.createNewFile();
            }
            else if (logfile.exists()){
                System.out.println("Logfile exists on filesystem. Deletes previous one & creates new one.");
                logfile.delete();
                logfile.createNewFile();
            }
        }
        catch (Exception e){
            System.out.println("Exception occured during creating log file: " + e);
        }

    }

    public void storeEvents(String log){
        try {
            FileWriter fileWriter = new FileWriter(logfile,true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(log);  //New line
            printWriter.close();
        }
        catch (Exception e) {System.out.println("Exception occured during flushing into log file: " + e);}
    }

    @OnTransition(source = "UNPAID", target = "WAITING_FOR_RECEIVE")
    public boolean payEventTransition() {
        System.out.println("TRANSITION1");
        storeEvents(getTimeStamp() + " >>>>> " +"UNPAID STATE...pay event...WAITING STATE");
        return true;
    }

    @OnTransition(source = "WAITING_FOR_RECEIVE", target = "DONE")
    public boolean receiveEventTransition() {
        System.out.println("TRANSITION2");
        storeEvents(getTimeStamp() + " >>>>> " + "WAITING STATE...receive event...DONE STATE");
        return true;
    }

    @OnTransition(source = "DONE", target = "UNPAID")
    public boolean startfromscratchEventTransition() {
        System.out.println("TRANSITION3");
        storeEvents(getTimeStamp() + " >>>>> " + "DONE STATE...startfromscratch event...UNPAID STATE");
        return true;
    }


}

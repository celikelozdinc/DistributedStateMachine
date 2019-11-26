package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

import java.util.Calendar;

@Service
public class RouterService {

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    public void setCheckpoint(String context) {
        CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), context);
        dbObjectHandler.insertCheckpoint(dbObject);
    }

    public String getCheckpoint(){return "N/A";}

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
}

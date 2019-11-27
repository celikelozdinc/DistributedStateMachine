package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Service
public class RouterService {

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    public void setCheckpoint(Message<String> ckptMessage) {
        String context = ckptMessage.getHeaders().get("context").toString();
        String machineUuid = ckptMessage.getHeaders().get("machineId").toString();
        System.out.printf("***** CONTEXT IS: %s\n",context);
        System.out.printf("***** UUID IS: %s\n",machineUuid);
        CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), context);
        dbObjectHandler.insertCheckpoint(dbObject);
    }

    public String getCheckpoint(Message<String> dummyPayload){
        String dummy = dummyPayload.getPayload();
        return dummy;
        /*
        List<CheckpointDbObject> list = dbObjectHandler.getAllCheckpoints();
        String ts = list.get(0).timestamp.toString();
        System.out.printf("Timestamp of first db object: %s\n",ts);
        return ts;
         */

    }

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

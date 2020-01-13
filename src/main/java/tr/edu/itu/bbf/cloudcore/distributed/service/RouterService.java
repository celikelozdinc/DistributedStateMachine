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
        /* Get state machine UUID */
        Object O_UUID = ckptMessage.getHeaders().get("machineId");
        UUID uuid = UUID.fromString(O_UUID.toString());
        /* Get processed event */
        String processedEvent = ckptMessage.getHeaders().get("processedEvent").toString();
        /* Get source and target states from StateContext */
        String sourceState  =  ckptMessage.getHeaders().get("source").toString();
        String targetState =  ckptMessage.getHeaders().get("target").toString();
        /* Get SMOC context */
        String context = ckptMessage.getHeaders().get("context").toString();
        /* Insert to database */
        //CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), uuid, sourceState, processedEvent, targetState, context);
        CheckpointDbObject dbObject = new CheckpointDbObject(context);
        dbObjectHandler.insertCheckpoint(dbObject);
    }

    public List<CheckpointDbObject> getCheckpoint(Message<String> getMessage){
        /* Get state machine UUID
        Object O_UUID = getMessage.getHeaders().get("machineId");
        UUID uuid = UUID.fromString(O_UUID.toString());
         */
        /*Read all records from database*/
        return dbObjectHandler.getAllCheckpoints();
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

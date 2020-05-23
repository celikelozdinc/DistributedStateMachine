package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import itu.distributed.ObjectSizeFetcher;

@Service
public class RouterService {

    static final Logger logger = LoggerFactory.getLogger(RouterService.class);

    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;

    private ArrayList<CheckpointDbObject> ckptList;

    @PostConstruct
    public void init() {
        logger.info("Initializing a new list in order to store CKPTs in memory ...");
        ckptList = new ArrayList<CheckpointDbObject>();
    }

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
        /* Get eventNumber */
        Integer eventNumber = Integer.valueOf(ckptMessage.getHeaders().get("eventNumber").toString());
        /* Insert to database */
        CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), uuid, sourceState, processedEvent, targetState, context,eventNumber);
        //CheckpointDbObject dbObject = new CheckpointDbObject(context);
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

    public void storeCKPTInMemory(Message<String> ckptMessage){
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
        /* Get eventNumber */
        Integer eventNumber = Integer.valueOf(ckptMessage.getHeaders().get("eventNumber").toString());
        /* Store in memory */
        CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), uuid, sourceState, processedEvent, targetState, context,eventNumber);
        ckptList.add(dbObject);
        logger.info("#CKPTs after appending = {}",ckptList.size());
        logger.info("DbObject size = {}",ObjectSizeFetcher.getObjectSize(dbObject));
        long currMemoryFootprint = (ckptList.size()) * (ObjectSizeFetcher.getObjectSize(dbObject));
        logger.info("Current Memory Footprint via Instrumentation = {}", currMemoryFootprint);
    }

    public List<CheckpointDbObject> getCKPTsFromMemory(Message<String> getMessage){
        return ckptList;
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

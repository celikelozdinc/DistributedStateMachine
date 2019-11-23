package tr.edu.itu.bbf.cloudcore.distributed.entity;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObjectHandler;

import java.util.Calendar;
import java.util.UUID;

@Component
public class Subscriber implements MessageHandler {

    /*
    @Autowired
    private CheckpointDbObjectHandler dbObjectHandler;
    */

    /*
    @Autowired
    private Processor CKPTProcessor;
     */
    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        Object o_payload = message.getPayload();
        String payload = o_payload.toString();
        System.out.printf("Subscriber endpoint handles the message --> %s\n",payload);
        System.out.println("Message headers are below...");
        /*Get uuid */
        Object O_UUID = message.getHeaders().get("machineId");
        UUID uuid = UUID.fromString(O_UUID.toString());
        System.out.println("UUID -> " + uuid);
        /* Get processed event */
        Object O_event = message.getHeaders().get("processedEvent");
        String processedEvent = O_event.toString();
        System.out.println("Processed Event -> " + processedEvent);
        /* Get source and target states */
        Object O_source = message.getHeaders().get("source");
        String sourceState = O_source.toString();
        System.out.println("Source state -> " + sourceState);
        Object O_target = message.getHeaders().get("target");
        String targetState = O_target.toString();
        System.out.println("Target state -> " + targetState );
        /* Get context */
        Object O_context = message.getHeaders().get("context");
        String context = O_context.toString();
        System.out.println("SMOC CONTEXT IS BELOW...");
        System.out.println(context);
        /* Persist to mongodb */
        //Processor CKPTProcessor = new Processor();
        //CKPTProcessor.processCheckpoint(context);
        //CheckpointDbObject dbObject = new CheckpointDbObject(this.getTimeStamp(), context);
        //CheckpointDbObjectHandler dbObjectHandler =  new CheckpointDbObjectHandler();
        //dbObjectHandler.insertCheckpoint(dbObject);
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

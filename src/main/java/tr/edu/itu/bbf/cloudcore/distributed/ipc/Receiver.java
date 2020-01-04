package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.Application;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.service.ServiceGateway;
import org.springframework.messaging.Message;
import tr.edu.itu.bbf.cloudcore.distributed.service.StateMachineWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class Receiver {
    static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private ServiceGateway serviceGateway;

    @Autowired
    private StateMachineWorker worker;

    public Receiver(){
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
        logger.info(" +++++++++ CONSTRUCTOR of RECEIVER ++++++++++");
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
    }

    @RabbitListener(queues = "${QUEUE}")
    public String process(IpcMessage msg) throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        String ipAddr = localhost.getHostAddress();
        String hostname = localhost.getHostName();
        logger.info("Receiver::process()");
        logger.info("Ip Addr of receiver  = {}",ipAddr);
        logger.info("Hostname of receiver = {}",hostname);
        logger.info("IpcMessage Received from sender. Hostname of sender={}, IP of sender={}",msg.getHostname(),msg.getIpAddr());
        logger.info(" +++++ Receiver:: READ FROM DATABASE +++++");
        Message<String> getMessage = MessageBuilder
                .withPayload("PAYLOAD")
                .build();
        List<CheckpointDbObject> list = serviceGateway.getCheckpoint(getMessage);
        if(list!=null && !list.isEmpty()){
            CheckpointDbObject dbObject = list.get(0);
            logger.info(" +++++ Source state = {}\n", dbObject.getSourceState());
            logger.info(" +++++ Processed event = {}\n", dbObject.getProcessedEvent());
            logger.info(" +++++ Target state = {}\n", dbObject.getTargetState());
            logger.info(" +++++ Context = {}\n",dbObject.getContext());
            logger.info(" +++++ Receiver:: READ FROM DATABASE +++++");
            //return dbObject.getContext();
            return "---> Receiver is "+hostname ;
        }
        else{
            return "NO_CKPT";
        }
    }

    @RabbitListener(queues = "${EVENT_QUEUE}")
    public String handleEvent(EventMessage msg) throws Exception {
        logger.info("***************");
        logger.info("***************");
        logger.info("Message received from __{}__ process.",msg.getSender());
        String event = msg.getEvent();
        String hostname = System.getenv("HOSTNAME");
        /* sleep time is parametrized */
        int timeSleep = Integer.parseInt(System.getProperty("timesleep"));
        worker.ProcessEvent(event,timeSleep);
        //worker.MarkCKPT();
        /* Sleep for 2 seconds */
        sleep((long) 2);
        String reply = "This is reply from " + hostname + " after event " + event;
        logger.info("Send this message back to smoc __{}__",reply);
        logger.info("***************");
        logger.info("***************");
        return reply;
    }


    public void sleep(Long sleepTime){
        try {
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Exception during sleep in main program --> " + ex.toString());
        }

    }

}


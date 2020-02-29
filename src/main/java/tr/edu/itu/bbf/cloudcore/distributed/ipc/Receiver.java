package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointDbObject;
import tr.edu.itu.bbf.cloudcore.distributed.service.ServiceGateway;
import org.springframework.messaging.Message;
import tr.edu.itu.bbf.cloudcore.distributed.service.StateMachineWorker;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
        logger.info(" +++++++++ CONSTRUCTOR of RECEIVER ++++++++++");
    }

    @PostConstruct
    public void init() {
        logger.info(" +++++++++ POSTCONTRUCT of RECEIVER ++++++++++");
    }

    @RabbitListener(queues = "${QUEUE}")
    public ArrayList<Response> process(CkptMessage msg) throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        String ipAddr = localhost.getHostAddress();
        String hostname = localhost.getHostName();
        logger.info("Receiver::process()");
        logger.info("Ip Addr of receiver  = {}",ipAddr);
        logger.info("Hostname of receiver = {}",hostname);
        logger.info("CkptMessage Received from sender. Hostname of sender={}, IP of sender={}",msg.getHostname(),msg.getIpAddr());
        logger.info(" +++++ Receiver:: READ FROM DATABASE +++++");
        Message<String> getMessage = MessageBuilder
                .withPayload("PAYLOAD")
                .build();
        /*
        List<CheckpointDbObject> list = serviceGateway.getCheckpoint(getMessage);
        logger.info("#CKPTs returned from database = {}",list.size());
         */

        List<CheckpointDbObject> list = serviceGateway.getCKPTsFromMemory(getMessage);
        logger.info("#CKPTs returned  = {}",list.size());

        ArrayList<Response> responseList = new ArrayList<>();
        if(list!=null && !list.isEmpty()){
            for(CheckpointDbObject dbObject: list){
                logger.info(" +++++ Event number = {}\n", dbObject.getEventNumber());
                logger.info(" +++++ Source state = {}\n", dbObject.getSourceState());
                logger.info(" +++++ Processed event = {}\n", dbObject.getProcessedEvent());
                logger.info(" +++++ Target state = {}\n", dbObject.getTargetState());
                logger.info(" +++++ Context = {}\n",dbObject.getContext());
                logger.info(" +++++ Receiver:: READ FROM DATABASE +++++");
                Response response = new Response(dbObject.getEventNumber(),dbObject.getSourceState(),dbObject.getProcessedEvent(),dbObject.getTargetState());
                responseList.add(response);
            }
            /*
            return "---> SMOC context  is "+dbObject.getContext() ;
            return "---> Receiver is "+hostname ;
             */
        }
        else{
           logger.info(" ---- EMPTY CKPT LIST WILL BE RETURNED ----");
        }
        return responseList;
    }

    @RabbitListener(queues = "${EVENT_QUEUE}")
    public String handleEvent(EventMessage msg) throws Exception {
        logger.info("***************");
        logger.info("***************");
        logger.info("Message received from __{}__ process.",msg.getSender());
        String event = msg.getEvent();
        String hostname = System.getenv("HOSTNAME");
        Integer eventNumber = msg.getEventNumber();
        /* sleep time is parametrized */
        int timeSleep = Integer.parseInt(System.getProperty("timesleep"));
        //pass timeSleep = 0
        String reply  = worker.ProcessEvent(event,eventNumber,0);
        //String reply = "This is reply from distributedstatemachine_" + hostname + " after event " + event;
        //logger.info("Send this message back to smoc __{}__",reply);
        logger.info("Sending reply to ___{}___ process.",msg.getSender());
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


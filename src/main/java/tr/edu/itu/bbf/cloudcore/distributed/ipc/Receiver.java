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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

@Component
public class Receiver {
    static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private ServiceGateway serviceGateway;

    public void Receiver(){
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
        logger.info(" +++++++++ CONSTRUCTOR of RECEIVER ++++++++++");
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
    }

    @RabbitListener(queues = "${smoc.rabbitmq.ckpt.queue}")
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
        CheckpointDbObject dbObject = list.get(0);
        logger.info(" +++++ Source state = {}\n", dbObject.getSourceState());
        logger.info(" +++++ Processed event = {}\n", dbObject.getProcessedEvent());
        logger.info(" +++++ Target state = {}\n", dbObject.getTargetState());
        logger.info(" +++++ Context = {}\n",dbObject.getContext());
        logger.info(" +++++ Receiver:: READ FROM DATABASE +++++");
        logger.info("Receiver returns CONTEXT to sender...");
        return dbObject.getContext();
    }

}


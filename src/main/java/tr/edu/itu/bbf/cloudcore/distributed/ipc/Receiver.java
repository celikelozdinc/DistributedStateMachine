package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
@Component
public class Receiver {
    static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    public void Receiver(){
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
        logger.info(" +++++++++ CONSTRUCTOR of RECEIVER ++++++++++");
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
    }

    @RabbitListener(queues = "${smoc.rabbitmq.ckpt.queue}")
    public String process(Message msg) throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        String ipAddr = localhost.getHostAddress();
        String hostname = localhost.getHostName();
        logger.info("Receiver::process()");
        logger.info("Ip Addr of receiver  = {}",ipAddr);
        logger.info("Hostname of receiver = {}",hostname);
        logger.info("Message Received from sender. Hostname of sender={}, IP of sender={}",msg.getHostname(),msg.getIpAddr());
        logger.info("Receiver returns a message to sender...");
        return "ACKNOWLEDGE";
    }

}

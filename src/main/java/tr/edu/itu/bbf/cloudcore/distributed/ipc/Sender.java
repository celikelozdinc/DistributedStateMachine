package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class Sender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${smoc.rabbitmq.ckpt.queue}")
    private String IPC_QUEUE;

    @Value("${smoc.rabbitmq.ckpt.exchange}")
    private String IPC_EXCHANGE;

    @Value("${smoc.rabbitmq.ckpt.routingkey}")
    private String IPC_ROUTING_KEY;

    static final Logger logger = LoggerFactory.getLogger(Sender.class);

    public void Sender(){
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
        logger.info(" +++++++++ CONSTRUCTOR of SENDER ++++++++++++");
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++");
    }

    public String send() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        String ipAddr = localhost.getHostAddress();
        String hostname = localhost.getHostName();
        logger.info("Sender class::send()");
        logger.info("Ip Addr of sender = {}",ipAddr);
        logger.info("Hostname of sender  = {}",hostname);
        IpcMessage msg = new IpcMessage();
        msg.setHostname(hostname);
        msg.setIpAddr(ipAddr);
        String reply = (String) rabbitTemplate.convertSendAndReceive(IPC_EXCHANGE,"rpc",msg);
        logger.info("Response from receiver = {}",reply);
        return reply;
    }
}

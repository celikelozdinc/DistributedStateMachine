package tr.edu.itu.bbf.cloudcore.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tr.edu.itu.bbf.cloudcore.distributed.persist.CheckpointRepository;
import tr.edu.itu.bbf.cloudcore.distributed.service.StateMachineWorker;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.util.*;

@SpringBootApplication
@ImportResource({"classpath*:channel-config.xml"})
@PropertySource(value={"classpath:application.properties"})
@ComponentScan(basePackages = {"tr.edu.itu.bbf.cloudcore.distributed"})
@EnableMongoRepositories(basePackageClasses=CheckpointRepository.class)
public class Application implements CommandLineRunner {

    /*
    @Autowired
    private static ServiceGateway serviceGateway;

    @Autowired
    private Sender sender;
    */

    static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private StateMachineWorker worker;

    @Override
    public void run(String... args) throws Exception {
        /* Reads timesleep argument
        *  which reads from `Program Arguments` section in jar configuration
        *String[] argument = args[0].split("=");
        *int timeSleep = Integer.parseInt(argument[1]);
        */

        /* Reads named arguments from `VM Options` section in jar configuration */
        int timeSleep = Integer.parseInt(System.getProperty("timesleep"));
        /*
        String type = System.getProperty( "type" );
        System.out.println("TYPE of SMOC is: " + type);
         */

        /*
        ApplicationContext context = new ClassPathXmlApplicationContext("channel-config.xml");
        serviceGateway = (ServiceGateway) context.getBean("serviceGateway");
         */

        /*
        String hostname = System.getenv("HOSTNAME");
        if (hostname.equals("smoc4")){
            String exchange = "";
            String reply = "";
            System.out.println(" --------------------------------");
            System.out.println("SMOC4 will try to make RPC call to other smocs.");
            // Hangi smoc'tan CKPT isteyeceğine karar ver.
            // Sonra, onun exchange ile send çağrısı yap
            exchange="SMOC2_CKPT_EXCHANGE";
            reply = sender.send(exchange);
            System.out.println("********* Response from receiver = " + reply);
            System.out.println(" --------------------------------");
            exchange="SMOC3_CKPT_EXCHANGE";
            reply = sender.send(exchange);
            System.out.println("********* Response from receiver = " + reply);
            System.out.println(" --------------------------------");
        }
        */

        /* Read hostname from ENV of SMOC */
        String hostname = System.getenv("HOSTNAME");
        if (hostname.equals("smoc2000000")){
            logger.warn("{} read CKPTs from other smocs...",hostname);
            /* Read CKPT information from other smocs */

            long startTime = System.currentTimeMillis();
            try {
                worker.startCommunication();
                worker.prepareCkpts();
                worker.applyCkpts();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            float delta =((float) (endTime - startTime)/1000);
            logger.info("Applied all CKPTs in {} seconds",delta);
        }
        else {
            InputStream stream = System.in;
            Scanner scanner = new Scanner(stream);

            while (true) {
                System.out.println("Waiting events to be processed...");
                String event = scanner.next();
            }

        }

        /*
        try {
            while (true) {
                System.out.print("Event:");
                String event = scanner.next();
                System.out.println("This event will be processed: " + event);
                ProcessEvent(event, timeSleep);
                sleep((long) 5);
                MarkCheckpoint();
                ReadMarkedCheckpoints();
                PrintCurrentStatus();
                // Can get, but can not set extendedstate variables
            }
        }catch(IllegalStateException e) {
            System.out.println("Exiting main loop...");
            e.printStackTrace();
        }
        */


        /*
        if (type.equals("sender")){
            sendPayEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after Pay event for sender is finished.");
            sendStartFromScratchEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after StartFromScratch event for sender is finished.");
            sendReceiveEvent(timeSleep);
            System.out.println("*****TIMESLEEP after Receive event for sender is finished.");
        }
        else if (type.equals("receiver")){
            sendReceiveEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after Receive event for receiver is finished.");
            sendPayEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after Pay event for receiver is finished.");
            sendStartFromScratchEvent(timeSleep);
            sleep((long) 30);
            System.out.println("*****TIMESLEEP after StartFromScratch event for receiver is finished.");
        }
        */
        /* LOOP 1*/
        /*
        sendPayEvent(timeSleep);
        sync(type);
        sendReceiveEvent(timeSleep);
        sendStartFromScratchEvent(timeSleep);
         */
        /* LOOP 2*/
        /*
        sendPayEvent(timeSleep);
        sendReceiveEvent(timeSleep);
        sync(type);
        sendStartFromScratchEvent(timeSleep);
         */
        /* LOOP 3 */
        /*
        sendPayEvent(timeSleep);
        sync(type);
        sendReceiveEvent(timeSleep);
        sync(type);
        sendStartFromScratchEvent(timeSleep);
         */
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /*
    public void PrintCurrentStatus() throws Exception{
        System.out.println("PrintCurrentStatus()::READ FROM ENSEMBLE");
        StateMachineContext<States, Events> context = stateMachineEnsemble.getState();
        System.out.println("PROCESSED EVENT IS " + context.getEvent().toString());
        System.out.println("AFTER EVENT, STATE IS " + context.getState().toString());
        System.out.println("AFTER EVENT, common and local variables are below:");
        ExtendedState  extendedState = context.getExtendedState();
        System.out.println("Common variable between events: " + extendedState.get("common", Integer.class) );
        System.out.println("Local variable for Waiting State: " + extendedState.get("localVarForWaiting", Integer.class));
        System.out.println("Local variable for Done State: " + extendedState.get("localVarForDone",Integer.class));
        System.out.println("PrintCurrentStatus()::READ FROM MONGODB");
        Message<String> getMessage = MessageBuilder
                .withPayload("PAYLOAD")
                .build();
        List<CheckpointDbObject> list = serviceGateway.getCheckpoint(getMessage);
        if (list != null && !list.isEmpty()) {
            System.out.println("# checkpoints inserted on database = " + list.size());
            for (CheckpointDbObject dbObject : list) {
                System.out.printf("Source state: %s\n", dbObject.getSourceState());
                System.out.printf("Processed event: %s\n", dbObject.getProcessedEvent());
                System.out.printf("Target state: %s\n", dbObject.getTargetState());
            }
        }
    }
    */

    /*
    public void ReadMarkedCheckpoints() throws Exception {
        List<String> paths = new ArrayList<String>();
        paths.add("/smoc1");
        paths.add("/smoc2");
        paths.add("/smoc3");
        paths.add("/smoc3");
        for(String path: paths) {
            if(sharedCuratorClient.checkExists().forPath(path)!=null) {
                byte[] bytes = sharedCuratorClient.getData().forPath(path);
                System.out.println("---ReadMarkedCheckpoints()::" + "PATH="+ path + "::DATA=" + new String(bytes));
            }

        }
    }
    */

}


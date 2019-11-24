package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class OutputService {

    @ServiceActivator
    public void printValue(String value){
        System.out.println(value);
    }
}

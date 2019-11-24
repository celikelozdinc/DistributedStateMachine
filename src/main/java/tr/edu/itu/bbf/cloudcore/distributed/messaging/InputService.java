package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.stereotype.Component;

@Component
public class InputService {

    public String ckpt(String name) {
        return "Hello " + name + "!";
    }
}

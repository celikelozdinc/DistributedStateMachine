package tr.edu.itu.bbf.cloudcore.distributed.messaging;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class InputService {

    public String ckpt(String context) {
        return context;
    }
}

package tr.edu.itu.bbf.cloudcore.distributed.service;

import org.springframework.stereotype.Service;

@Service
public class RouterService {

    public void setCheckpoint(String context) {
        System.out.printf("SMOC context is : %s\n",context);
    }

    public String getCheckpoint(){return "N/A";}
}

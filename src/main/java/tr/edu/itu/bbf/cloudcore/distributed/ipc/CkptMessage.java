package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import java.io.Serializable;
import java.util.UUID;

public class CkptMessage implements Serializable {

    public String ipAddr;
    public String hostname;
    public UUID smocUuid;

    public CkptMessage(){}

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public UUID getSmocUuid() {
        return smocUuid;
    }

    public void setSmocUuid(UUID smocUuid) {
        this.smocUuid = smocUuid;
    }
}
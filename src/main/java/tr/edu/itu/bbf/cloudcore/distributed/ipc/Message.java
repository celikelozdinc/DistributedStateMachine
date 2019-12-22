package tr.edu.itu.bbf.cloudcore.distributed.ipc;

import java.io.Serializable;

public class Message implements Serializable {

    public String ipAddr;
    public String hostname;

    public Message(){}

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

}
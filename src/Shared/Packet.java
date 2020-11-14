package Shared;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID  = 1L;
    private int type;
    private String data1;
    private String clientID;
    private String destID;

    public Packet(int type, String data1, String clientID, String destID) {
        this.type = type;
        this.data1 = data1;
        this.clientID = clientID;
        this.destID = destID;
    }
}

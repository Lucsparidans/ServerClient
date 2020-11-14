package Shared;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID  = 1L;
    private int type;
    private String senderID;
    private String destID;
    private String data;
    private String firstName;
    private String lastName;
    private String publicKey;

    public Packet(int type, String senderID, String destID, String data, String firstName, String lastName, String publicKey) {
        this.type = type;
        this.senderID = senderID;
        this.destID = destID;
        this.data = data;
        this.firstName = firstName;
        this.lastName = lastName;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return String.format(
                "Packet[Type:%d\nSenderID: %s\nDestinationID: %s\nData: %s\nFirstName: %s\nLastName: %s\nPublicKey: %s]\n",
                type,senderID,destID,data,firstName,lastName,publicKey);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getDestID() {
        return destID;
    }

    public void setDestID(String destID) {
        this.destID = destID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}

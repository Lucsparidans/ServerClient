package Shared;

import java.io.Serializable;

public class Packet implements Serializable {

    public enum PacketType {
        SYN,
        ACK,
        SYN_ACK,
        MSG_REQUEST,
        MSG,
        RECEIVED_CONFIRM,
        NO_MSGs,
        UNKNOWN_USER_ERROR,
        ERROR
    }
    public enum DataFormat{
        STRING,
        MESSAGE,
        ARRAYLIST_MESSAGES
    }

    // region Variables
    private static final long serialVersionUID = 1L;
    private PacketType type;
    private final DataFormat dataFormat;
    private String senderID;
    private String destID;
    private Object data;
    private String firstName;
    private String lastName;
    private String publicKey;

    // endregion

    public Packet(PacketType type, String senderID, String destID, Object data, DataFormat dataFormat,String firstName, String lastName, String publicKey) {
        this.type = type;
        this.dataFormat = dataFormat;
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
                "Packet[Type:%s\nSenderID: %s\nDestinationID: %s\nData: %s\nFirstName: %s\nLastName: %s\nPublicKey: %s]\n",
                type, senderID, destID, data, firstName, lastName, publicKey);
    }
    // region Getters/Setters

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public boolean hasName() {
        return this.firstName != null && this.getLastName() != null;
    }

    public String getFullName() {
        return this.firstName.concat(" ".concat(this.lastName));
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

    public Object getData() {
        return data;
    }

    public DataFormat getDataFormat(){
        return dataFormat;
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

    // endregion
}

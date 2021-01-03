package Client;

public class Action {
    private String type;
    private String toID;
    private String fromID;
    private String message;
    private String senderID;

    public Action(String type, String toID, String message) {
        this.type = type;
        this.toID = toID;
        this.message = message;
        this.senderID = null;
    }

    public Action(String type, String fromID, String toID, String message){
        this(type,toID,message);
        this.fromID = fromID;
        this.senderID = null;
    }

    public Action(String type, String fromID, String toID, String message, String senderID){
        this(type,fromID,toID,message);
        this.senderID = senderID;
    }

    public String getToID() {
        return toID;
    }

    public String getMessageAsString() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getFromID(){return fromID;}

    public String getSenderID() {
        return senderID;
    }

    @Override
    public String toString() {
        return String.format("Action: type %s, fromID %s, toID %s, data %s", type, fromID, toID, message);
    }
}

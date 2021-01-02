package Client;

public class Action {
    private String type;
    private String toID;
    private String fromID;
    private String message;

    public Action(String type, String toID, String message) {
        this.type = type;
        this.toID = toID;
        this.message = message;
    }

    public Action(String type, String fromID, String toID,String message){
        this(type,toID,message);
        this.fromID = fromID;
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
}

package Client;

public class Action {
    private String type;
    private String toID;
    private String message;

    public Action(String type, String toID, String message) {
        this.type = type;
        this.toID = toID;
        this.message = message;
    }

    public String getToID() {
        return toID;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}

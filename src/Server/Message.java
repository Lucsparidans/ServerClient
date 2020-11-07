package Server;

public class Message {
    private String fromId;
    private String toId;
    private String message;

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(String fromId, String toId, String message) {
        this.fromId = fromId;
        this.toId = toId;
        this.message = message;
    }
}

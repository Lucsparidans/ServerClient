package Server;

public class ClientInfo {
    private String id;
    private String firstName;
    private String lastName;
    private String publicKey;

    public ClientInfo(String id, String firstName, String lastName, String publicKey ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.publicKey = publicKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

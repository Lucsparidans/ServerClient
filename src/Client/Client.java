package Client;

import Server.Message;
import Shared.Packet;
import Shared.Packet.DataFormat;
import Shared.Packet.PacketType;
import Shared.PacketLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable{

    private final PacketLogger pktLog;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private Socket s;

    public enum ClientIDType{
        NAME,
        ID
    }
    // region Client Information
    // general
    private String duration;
    private String retries;

    private String timeout;
    // person
    private String id;
    private String name;
    private String privateKey;

    private String publicKey;
    // server
    private String ip;
    private String port;

    private static final int PORT = 4444;
    // actions
    private final ArrayList<Action> actions;

    // endregion

    public Client(String file) {
        pktLog = new PacketLogger();
        s = null;
        actions = new ArrayList<>();
        // Parse the JSON file and connect to the server
        try {
            parseJSON(file);
            s = connectToServer(InetAddress.getLoopbackAddress(),PORT);
            System.out.println("Socket connected");
        } catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            objectInputStream = new ObjectInputStream(s.getInputStream());
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());

            boolean verified = false;
            System.out.println("Start clientside verification");
            while (!verified) {
                objectOutputStream.writeObject(pktLog.newOut(
                        new Packet(PacketType.SYN,
                                id,
                                null,
                                null,
                                null,
                                name.split(" ")[0],
                                name.split(" ")[1],
                                publicKey)));
                Packet p = pktLog.newIn(objectInputStream.readObject());
                if (p != null) {
                    if (p.getType() == PacketType.ACK) {
                        verified = true;
                        objectOutputStream.writeObject(pktLog.newOut(
                                new Packet(
                                        PacketType.SYN_ACK,
                                        id,
                                        null,
                                        null,
                                        null,
                                        name.split(" ")[0],
                                        name.split(" ")[1],
                                        publicKey
                                )));
                    }
                    p = pktLog.newIn(objectInputStream.readObject());
                    if(p.getType() == PacketType.RECEIVED_CONFIRM) System.out.println("ACK");
                } else
                    Thread.sleep(Integer.parseInt(timeout) * 1000L);
            }

        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that connects the client to a server at the specified address and port
     *
     * @param hostAddr address of the server
     * @param port     Server port
     * @return new socket that is connected to the server
     * @throws InterruptedException Interruption on the Thread.sleep
     */
    private Socket connectToServer(InetAddress hostAddr, int port) throws InterruptedException {
        Socket sock = null;
        while (sock == null) {
            // REGISTRATION
            try {
                sock = new Socket(hostAddr, port);
            } catch (ConnectException e) {
                System.out.println("Timeout on connection request"); //Server was not open yet probably
                Thread.sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sock;
    }

    @Override
    public void run() {
        System.out.println("Started client");
        long endTime = System.currentTimeMillis() + Long.parseLong(this.duration) * 1000L;
        while(System.currentTimeMillis() < endTime){
            checkMessages();
            executeAction();
        }

        System.out.println("duration over");
        socketClose();
        System.out.println(pktLog.getLoggedSequence());
    }

    /**
     * Method that parses the JSON file and saves the included information
     *
     * @param file Path to the JSON file
     * @throws FileNotFoundException File was not found
     * @throws IOException           IO went wrong
     * @throws ParseException        Could not parse the JSON file
     */
    private void parseJSON(String file) throws FileNotFoundException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(file));

        // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
        JSONObject jsonObject = (JSONObject) obj;

        // A JSON array. JSONObject supports java.util.List interface.
        JSONObject general = (JSONObject) jsonObject.get("general");
        this.duration = (String) general.get("duration");
        this.retries = (String) general.get("retries");
        this.timeout = (String) general.get("timeout");

        JSONObject person = (JSONObject) jsonObject.get("person");
        this.id = (String) person.get("id");
        String tmpName = (String) person.get("name");
        String[] parts = tmpName.split(",");
        this.name = parts[1].trim() + " " + parts[0];

        JSONObject keys = (JSONObject) person.get("keys");
        this.privateKey = (String) keys.get("private");
        this.publicKey = (String) keys.get("public");

        JSONObject server = (JSONObject) jsonObject.get("server");
        this.ip = (String) server.get("ip");
        this.port = (String) server.get("port");

        parseActions((JSONArray) jsonObject.get("actions"));
    }

    private void parseActions(JSONArray actions){
        for (Object action : actions) {

            String[] parts = action.toString().split("\\[", 3);

            String actionType = parts[0];
            actionType = actionType.replace(" ", "");

            String toId = parts[1];
            toId = toId.replace("] ", "");

            String m = parts[2];
            m = m.replace("]", "");

            this.actions.add(new Action(actionType,toId,m));
        }
    }

    private void executeAction(){
        if(!this.actions.isEmpty()){
            System.out.println("Executing action");
            Action action = this.actions.get(0);
            this.actions.remove(0);
            if(action.getType().equals("SEND")){
                Packet p;
                if(!action.getToID().contains(",")){
                    p = new Packet(PacketType.MSG,
                            id,
                            action.getToID(),
                            action.getMessage(),
                            DataFormat.MESSAGE,
                            null,
                            null,
                            null);
                }
                else{
                    String[] name = action.getToID().split(",", 2);
                    String firstName = name[1].trim();
                    String lastName = name[0].trim();
                    p = new Packet(PacketType.MSG,
                            id,
                            null,
                            action.getMessage(),
                            DataFormat.MESSAGE,
                            firstName,
                            lastName,
                            null);
                }
                boolean succes = sendEncryptedMessage(p);
                // Handle !success
            }
        }
    }

    private boolean sendEncryptedMessage(Packet packet){
        // TODO: Request key
        // TODO: Encrypt
        // TODO: Send (Using the retries)
        // TODO: Receive and verify confirmation from server
        try {
            objectOutputStream.writeObject(pktLog.newOut(packet));
            Packet p = pktLog.newIn(objectInputStream.readObject());
            if(p.getType() == PacketType.RECEIVED_CONFIRM){
                return true;
            }
            else if(p.getType() == PacketType.UNKNOWN_USER_ERROR){
                System.out.println("Attempted to send message to user unknown to the database!");
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    private void socketClose(){
        try {
            objectOutputStream.writeObject(pktLog.newOut(
                    new Packet(PacketType.CLOSE,
                            null,
                            null,
                            null,
                            null,
                            null ,
                            null,
                            null)));
            Packet p = pktLog.newIn(objectInputStream.readObject());
            if(p.getType()==PacketType.RECEIVED_CONFIRM){
                s.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    private void checkMessages(){
        // TODO: Send a packet to confirm receiving a packet for all incoming packets
        try{
            // Request messages from the server for this client
            objectOutputStream.writeObject(pktLog.newOut(
                    new Packet(PacketType.MSG_REQUEST,
                            id,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null)
            ));
            Packet conf = pktLog.newIn(objectInputStream.readObject());
            if(conf.getType() != PacketType.RECEIVED_CONFIRM){
                // TODO: Handle!
            }
            // Response from server
            Packet p = pktLog.newIn(objectInputStream.readObject());
            // Send confirmation of receiving the message
            objectOutputStream.writeObject(pktLog.newOut(new Packet(
                    PacketType.RECEIVED_CONFIRM,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            )));
            if(p.getType() != PacketType.NO_MSGs) {
                DataFormat dataFormat = p.getDataFormat();
                switch (dataFormat) {
                    case STRING:
                        System.out.printf("Message: %s\n", p.getData());
                        break;
                    case MESSAGE:
                        System.out.printf("Message: %s\n", ((Message) p.getData()).getMessage());
                        break;
                    case ARRAYLIST_MESSAGES:
                        ArrayList<?> messages = (ArrayList<?>) p.getData();
                        for (Object message : messages) {
                            Message m = (Message) message;
                            // TODO: Decrypt the data
                            System.out.printf("Message: %s\n", m.getMessage());
                        }
                        break;
                }
            }
            else System.out.println("No messages!"); // TODO: Change this
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

// region old
// EXECUTE ACTIONS
//        for (Object action : this.actions) {
//            if (actionType.equals("SEND")) {
//                if (!toId.contains(",")) {
//                    int tries = 0;
//                    boolean sent = false;
//                    while(tries < Integer.parseInt(retries) && !sent){
//                        objectOutputStream.writeObject(pktLog.newOut(
//                                new Packet(PacketType.MSG,
//                                        id,
//                                        toId,
//                                        m,
//                                        DataFormat.STRING,
//                                        null,
//                                        null,
//                                        null)));
//                        Packet p = (Packet)objectInputStream.readObject();
//                        if(p!=null) {
//                            if (p.getData().equals("Success message sent"))
//                                sent = true;
//                            else {
//                                tries = +1;
//                                Thread.sleep(Integer.parseInt(timeout)*1000L);
//                            }
//                        }
//                    }
//                } else {
//
//
//                    int tries = 0;
//                    boolean sent = false;
//                    while(tries < Integer.parseInt(retries) && !sent){
//                        objectOutputStream.writeObject(pktLog.newOut(
//                                new Packet(PacketType.MSG,
//                                        id,
//                                        null,
//                                        m,
//                                        DataFormat.STRING,
//                                        firstName,
//                                        lastName,
//                                        null)));
//                        Packet p = (Packet)objectInputStream.readObject();
//                        if(p!=null) {
//                            if (p.getData().equals("Success message sent"))
//                                sent = true;
//                            else {
//                                tries = +1;
//                                Thread.sleep(Integer.parseInt(timeout) * 1000L);
//                            }
//                        }
//                    }
//                }
//            }
//        }
// endregion

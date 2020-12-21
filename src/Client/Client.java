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
import java.net.Socket;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Client {

    private final PacketLogger pktLog;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private Socket s;

    // Client Information
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

    // actions
    private final PriorityQueue<Action> actions;

    public Client(String file) {
        pktLog = new PacketLogger();
        s = null;
        actions = new PriorityQueue<Action>();
        // Parse the JSON file and connect to the server
        try {
            parseJSON(file);
            s = connectToServer("localhost", 4999);
        } catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            objectInputStream = new ObjectInputStream(s.getInputStream());
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());

            boolean verified = false;
            while (!verified) {
                objectOutputStream.writeObject(pktLog.newOut(
                        new Packet(PacketType.SYN,
                                id,
                                null,
                                null,
                                null,
                                name,
                                name,
                                publicKey)));
                Packet p = (Packet) objectInputStream.readObject();
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
                                        name,
                                        name,
                                        publicKey
                                )));
                    }
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
    private Socket connectToServer(String hostAddr, int port) throws InterruptedException {
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

    public void startClient() throws IOException, ClassNotFoundException, InterruptedException {
        long endTime = System.currentTimeMillis() + Long.parseLong(this.duration) * 1000L;

        while(System.currentTimeMillis() < endTime){
            checkMessages();
            executeAction();
        }

        System.out.println("duration over");
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
        this.name = (String) person.get("name");

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
            Action action = this.actions.poll();
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
                    String firstName = name[0].replace(" ", "");
                    String lastName = name[1].replace(" ", "");
                    p = new Packet(PacketType.MSG,
                            id,
                            null,
                            action.getMessage(),
                            DataFormat.MESSAGE,
                            firstName,
                            lastName,
                            null);
                }
                sendEncryptedMessage(p);
            }
        }
    }

    private void sendEncryptedMessage(Packet packet){
        // TODO: Request key
        // TODO: Encrypt
        // TODO: Send (Using the retries)
    }
    private void checkMessages(){
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
            // Response from server
            Packet p = pktLog.newIn(objectInputStream.readObject());
            DataFormat dataFormat = p.getDataFormat();
            switch (dataFormat){
                case STRING:
                    System.out.printf("Message: %s\n",p.getData());
                    break;
                case MESSAGE:
                    System.out.printf("Message: %s\n",((Message)p.getData()).getMessage());
                    break;
                case ARRAYLIST_MESSAGES:
                    ArrayList<?> messages = (ArrayList<?>) p.getData();
                    for(Object message : messages){
                        Message m = (Message) message;
                        // TODO: Decrypt the data
                        System.out.printf("Message: %s\n",m.getMessage());
                    }
                    break;
            }
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

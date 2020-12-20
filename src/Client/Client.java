package Client;

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
    private JSONArray actions;

    public Client(String file) {
        pktLog = new PacketLogger();
        s = null;

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

            // Read message from Server
            // TODO: Find out what this is doing???
//            pktLog.newIn(objectInputStream.readObject());
//            System.out.println(pktLog.getLastIn().getData());
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

        this.actions = (JSONArray) jsonObject.get("actions");
    }

    public void startClient() throws IOException, ClassNotFoundException, InterruptedException {
        long startingTime = System.currentTimeMillis();

        // EXECUTE ACTIONS
        for (Object action : this.actions) {

            String a = action.toString();
            String[] parts = a.split("\\[", 3);

            String actionType = parts[0];
            actionType = actionType.replace(" ", "");

            String toId = parts[1];
            toId = toId.replace("] ", "");

            String m = parts[2];
            m = m.replace("]", "");

            if (actionType.equals("SEND")) {
                if (!toId.contains(",")) {
                    int tries = 0;
                    boolean sent = false;
                    while(tries < Integer.parseInt(retries) && !sent){
                        objectOutputStream.writeObject(pktLog.newOut(
                                new Packet(PacketType.MSG, // TODO: With ID
                                        id,
                                        toId,
                                        m,
                                        DataFormat.STRING,
                                        null,
                                        null,
                                        null)));
                        Packet p = (Packet)objectInputStream.readObject();
                        if(p!=null) {
                            if (p.getData().equals("Success message sent"))
                                sent = true;
                            else {
                                tries = +1;
                                Thread.sleep(Integer.parseInt(timeout)*1000L);
                            }
                        }
                    }
                } else {
                    String[] name = toId.split(",", 2);
                    String firstName = name[0].replace(" ", "");
                    String lastName = name[1].replace(" ", "");

                    int tries = 0;
                    boolean sent = false;
                    while(tries < Integer.parseInt(retries) && !sent){
                        objectOutputStream.writeObject(pktLog.newOut(
                                new Packet(PacketType.MSG, // TODO: With name
                                        id,
                                        null,
                                        m,
                                        DataFormat.STRING,
                                        firstName,
                                        lastName,
                                        null)));
                        Packet p = (Packet)objectInputStream.readObject();
                        if(p!=null) {
                            if (p.getData().equals("Success message sent"))
                                sent = true;
                            else {
                                tries = +1;
                                Thread.sleep(Integer.parseInt(timeout) * 1000L);
                            }
                        }
                    }
                }
            }
        }

        // CHECK FOR MESSAGES
        double milliSeconds = Double.parseDouble(this.duration) * 1000;
        System.out.println("checking for messages..");
        // Constantly checking for new messages
        while (System.currentTimeMillis() < startingTime + milliSeconds) {

            pktLog.newIn(objectInputStream.readObject());

            // Send get messages request to server
            objectOutputStream.writeObject(pktLog.newOut(
                    new Packet(PacketType.MSG_REQUEST,
                            id,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null)));
            try {
                pktLog.newIn(objectInputStream.readObject());
                PacketType messagesNumber = pktLog.getLastIn().getType();

                if (messagesNumber != PacketType.ERROR) {
                    // TODO: Fix this code?!
//                    for (int j = 0; j < messagesNumber; j++) {
//                        String message = pktLog.getLastIn().getData();
//                        System.out.println("Message " + j + " : " + message);
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("duration over");
    }
}

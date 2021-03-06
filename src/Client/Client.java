package Client;

import Server.Encryption;
import Shared.FileLogger;
import Shared.Message;
import Shared.Packet;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static Shared.ConsoleLogger.LogMessage;

public class Client implements Runnable{

    private static final boolean DEBUG = true;

    private final PacketLogger pktLog;

    private final ArrayList<String> receivedMessages;

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
        receivedMessages = new ArrayList<>();
        // Parse the JSON file and connect to the server
        try {
            parseJSON(file);
            s = connectToServer(InetAddress.getLoopbackAddress(),PORT);
            LogMessage("Socket connected");
        } catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            objectInputStream = new ObjectInputStream(s.getInputStream());
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());

            boolean verified = false;
            LogMessage("Start clientside verification");
            System.out.println();
            while (!verified) {
                objectOutputStream.writeObject(pktLog.newOut(
                        new Packet(PacketType.SYN,
                                id,
                                null,
                                null,
                                name.split(" ")[0].toUpperCase(),
                                name.split(" ")[1].toUpperCase(),
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
                                        name.split(" ")[0].toUpperCase(),
                                        name.split(" ")[1].toUpperCase(),
                                        publicKey
                                )));
                    }
                    p = pktLog.newIn(objectInputStream.readObject());
                    if(p.getType() == PacketType.RECEIVED_CONFIRM){
                        // NOPE
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
    private Socket connectToServer(InetAddress hostAddr, int port) throws InterruptedException {
        Socket sock = null;
        while (sock == null) {
            // REGISTRATION
            try {
                sock = new Socket(hostAddr, port);
            } catch (ConnectException e) {
                LogMessage("Timeout on connection request");
                Thread.sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sock;
    }

    @Override
    public void run() {
        LogMessage("Started client on thread: %s\n",Thread.currentThread().getName());
        long endTime;
        if(DEBUG){
            endTime = System.currentTimeMillis() + Long.parseLong(this.duration) * 100L;
        }
        else{
            endTime = System.currentTimeMillis() + Long.parseLong(this.duration) * 1000L;
        }
        while(System.currentTimeMillis() < endTime){
            checkMessages();
            executeAction();
        }

        LogMessage("Client on: %s closing down!\n  Cause: End of lifetime reached.\n", Thread.currentThread().getName());
        socketClose();

        // Print all logged packets
        FileLogger.writeLogToFile(pktLog.getLoggedSequence());
        FileLogger.writeMessagesToFile(receivedMessages,this.name);
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

    /**
     * Method that parses the actions which has to be processed and are stored in a JSONArray
     * @param actions JSONArray storing the different actions for a specific client
     */
    private void parseActions(JSONArray actions){
        for (Object action : actions) {
            String[] parts = action.toString().split(" ", 2);
            String actionType = parts[0];
            actionType = actionType.trim();

            String[] p2 = parts[1].split("]",2);

            String toId = p2[0];
            toId = toId.replace("[", "").trim();

            String m = p2[1];

            if(p2[1].charAt(1) == '['){
                m = m.replace("[","");
                m = m.replace("]","");
            }
            m = m.trim();

            this.actions.add(new Action(actionType,toId,m));
        }
    }

    /**
     * Method executing the current action of the client
     */
    private void executeAction(){
        if(!this.actions.isEmpty()){
            LogMessage("Executing action");
            Action action = this.actions.get(0);
            this.actions.remove(0);
            if(action.getType().equals("SEND")){
                Packet p;
                if(!action.getToID().contains(",")){
                    p = new Packet(PacketType.MSG,
                            id,
                            action.getToID(),
                            action.getMessageAsString(),
                            null,
                            null,
                            null);
                }
                else{
                    String[] name = action.getToID().split(",", 2);
                    String firstName = name[1].trim().toUpperCase();
                    String lastName = name[0].trim().toUpperCase();
                    p = new Packet(PacketType.MSG,
                            id,
                            null,
                            action.getMessageAsString(),
                            firstName,
                            lastName,
                            null);
                }
                boolean succes = sendEncryptedMessage(p);
                // Handle !success
            }
        }
    }

    /**
     * Boolean method sending an encrypted message if the client has to send a message after getting
     * the public key of the receiver to encrypt the message.
     * @param packet Packet storing information regarding a message.
     * @return true if the message is successfully transmitted and false if it is not.
     */
    private boolean sendEncryptedMessage(Packet packet){
        sendPacket(new Packet(PacketType.PUBLIC_KEY_REQUEST,
                null,
                packet.getDestID(),
                null,
                packet.getFirstName(),
                packet.getLastName(),
                null));
        Packet pKeyPacket = receivePacket();
        try {
            if(pKeyPacket.getType() == PacketType.PUBLIC_KEY_REQUEST) {
                objectOutputStream.writeObject(pktLog.newOut(new Packet(PacketType.RECEIVED_CONFIRM,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)));
            }
            else{
                // UNKNOWN USER ERROR
                LogMessage("Trying to send message to unknown user!");
                return false;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        String pKey = Objects.requireNonNull(pKeyPacket).getPublicKey();
        Object o = packet.getData();
        if(o instanceof String){
            packet.setData(Encryption.encrypt(pKey,(String)o));
        }else if(o instanceof Message){
            Message msg = (Message)o;
            msg.setMessage(Encryption.encrypt(pKey,msg.getMessage()));
            packet.setData(msg);
        }else if(o instanceof ArrayList){
            ArrayList<Message> encryptedMessages = new ArrayList<>();
            for (Object obj :
                    (ArrayList<?>) o){
                Message msg = (Message)obj;
                msg.setMessage(Encryption.encrypt(pKey,msg.getMessage()));
                encryptedMessages.add(msg);
            }
            packet.setData(encryptedMessages);
        }
        try {
            objectOutputStream.writeObject(pktLog.newOut(packet));
            Packet p = pktLog.newIn(objectInputStream.readObject());
            if(p.getType() != PacketType.RECEIVED_CONFIRM){
                // Handle!
            }
            p = pktLog.newIn(objectInputStream.readObject());
            if(p.getType() == PacketType.UNKNOWN_USER_ERROR){
                LogMessage("Attempted to send message to user unknown to the database!");
                return false;
            }
            else{
                return true;
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method to close a socket after the lifetime of the client has expired
     */
    private void socketClose(){
        try {
            objectOutputStream.writeObject(pktLog.newOut(
                    new Packet(PacketType.CLOSE,
                            null,
                            null,
                            null,
                            null ,
                            null,
                            null)));
            Packet p = pktLog.newIn(objectInputStream.readObject());
            if(p.getType()==PacketType.RECEIVED_CONFIRM){
                objectOutputStream.close();
                objectInputStream.close();
                s.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method checking if the client has any unread messages
     */
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
                            null)
            ));
            Packet conf = pktLog.newIn(objectInputStream.readObject());
            if(conf.getType() != PacketType.RECEIVED_CONFIRM){
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
                    null
            )));
            if(p.getType() != PacketType.NO_MSGs) {
                Object o = p.getData();
                String t = new SimpleDateFormat("dd-MM-yyyy,HH:mm").format(new Date());
                if(o instanceof String){
                    String message = t + "-> " + Encryption.decrypt(this.privateKey,(String)o);
                    receivedMessages.add(message);
                    LogMessage("Message %s\n", message);

                }else if(o instanceof Message){
                    String message = t + "-> " + Encryption.decrypt(this.privateKey,((Message) o).getMessage());
                    receivedMessages.add(message);
                    LogMessage("Message %s\n", Encryption.decrypt(this.privateKey,((Message) o).getMessage()));

                }else if(o instanceof ArrayList){
                    ArrayList<?> messages = (ArrayList<?>) o;
                    for (Object message : messages) {
                        Message m = (Message) message;
                        String decryptedMessage = t + "-> " + Encryption.decrypt(this.privateKey,m.getMessage());
                        receivedMessages.add(decryptedMessage);
                        LogMessage("Message %s\n", decryptedMessage);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method giving access to the different packets received by the client and adding it to the logs of Packets for the client
     * @return a Packet object just received
     */
    private Packet receivePacket(){
        try {
            return pktLog.newIn(objectInputStream.readObject());
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method sending packets to other clients,waiting for a confirmation and adding it to the logs of the client
     * @param p Packet object to be sent to another client
     */
    private void sendPacket(Packet p){
        try {
            objectOutputStream.writeObject(pktLog.newOut(p));
            Packet pIn = pktLog.newIn(objectInputStream.readObject());
            assert pIn.getType() == PacketType.RECEIVED_CONFIRM;
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
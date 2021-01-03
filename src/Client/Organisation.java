package Client;

import Shared.*;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static Server.Encryption.decrypt;
import static Server.Encryption.encrypt;
import static Shared.ConsoleLogger.LogMessage;
import static Shared.Packet.PacketType.RECEIVED_CONFIRM;

public class Organisation implements Runnable{

    private static final long TIMEOUT = 10L;

    private final PacketLogger pktLog;

    private final ArrayList<String> receivedMessages;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private MessageHandler messageHandler;
    private ActionHandler actionHandler;

    private Socket s;

    private boolean running;
    private final Object LOCK = new Object();
    private static final int PORT = 4444;
    private String name;

    private String privateKey;
    private String publicKey;
    private Double balance;


    public enum Role{
        CUSTOMER {
            @Override
            public String toString() {
                return "Customer";
            }
        },
        EMPLOYEE{
            @Override
            public String toString() {
                return "Employee";
            }
        },
        ADMIN{
            @Override
            public String toString() {
                return "Admin";
            }
        }
    }
    private final HashMap<String, Role> clients = new HashMap<>();
    private final HashMap<String, Boolean> checkIn = new HashMap<>();
    private final HashMap<String, Double> balances = new HashMap<>();
    private final LinkedBlockingQueue<Action> actions = new LinkedBlockingQueue<>();

    public Organisation(JSONObject organisation) {
        pktLog = new PacketLogger();
        s = null;
        receivedMessages = new ArrayList<>();
        KeyPairB64 keyPairB64 = KeyPairGenerator.generateRSAKeyPair();
        privateKey = keyPairB64.getPrivateKey();
        publicKey = keyPairB64.getPublicKey();
        try {
            parseJSON(organisation);
            s = connectToServer(InetAddress.getLoopbackAddress(),PORT);
            LogMessage("Socket connected");
        } catch (InterruptedException e) {
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
                        new Packet(Packet.PacketType.SYN,
                                name,
                                null,
                                null,
                                null,
                                null,
                                publicKey)));
                Packet p = pktLog.newIn(objectInputStream.readObject());
                if (p != null) {
                    if (p.getType() == Packet.PacketType.ACK) {
                        verified = true;
                        objectOutputStream.writeObject(pktLog.newOut(
                                new Packet(
                                        Packet.PacketType.SYN_ACK,
                                        name,
                                        null,
                                        null,
                                        null,
                                        null,
                                        publicKey
                                )));
                    }
                    p = pktLog.newIn(objectInputStream.readObject());
                    if(p.getType() == RECEIVED_CONFIRM) System.out.println("ACK");
                } else {
                    //Thread.sleep(Integer.parseInt(timeout) * 1000L);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void parseJSON(JSONObject org) {
        this.balance = Double.parseDouble((String) org.get("balance"));
        JSONArray roles = (JSONArray) org.get("roles");
        this.name = (String) org.get("name");
        JSONArray employees = (JSONArray) org.get("employees");

        for (Object o :
                employees) {
            JSONObject empl = (JSONObject) o;
            clients.put((String)empl.get("id"),
                    Role.valueOf(((JSONArray)empl.get("roles")).get(0).toString().toUpperCase()));
            checkIn.put((String)empl.get("id"),false);
        }
        balances.put(name, balance);
    }

    private Socket connectToServer(@NotNull InetAddress hostAddr, int port) throws InterruptedException {
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

    private Role getRole(String id){
        if(checkIn.containsKey(id)){
            if(checkIn.get(id)){
                return clients.get(id);
            }
        }
        return Role.CUSTOMER;
    }
    private boolean checkIn(String id){
        if(checkIn.containsKey(id)){
            checkIn.replace(id, true);
            return true;
        }
        return false;
    }
    private boolean checkOut(String id){
        if(checkIn.containsKey(id)){
            checkIn.replace(id, false);
            return true;
        }
        return false;
    }

    /**
     * Method that registers a new client into the bank
     *
     *
     * @param id
     * @param amount
     */
    private void register(String id, Double amount){
        if(!balances.containsKey(id)){
            balances.put(id, amount);
            if(!clients.containsKey(id)){
                clients.put(id, Role.CUSTOMER);
                sendPacket(new Packet(Packet.PacketType.MSG,
                        name,
                        id,
                        null,
                        null,
                        null,
                        null,
                        encrypt(requestPrivateKey(id),Role.CUSTOMER.toString())));
                Packet p = receivePacket();
                if(p.getType() != RECEIVED_CONFIRM){
                    // Do something
                }
            }
            else{
                sendPacket(new Packet(Packet.PacketType.MSG,
                        name,
                        id,
                        null,
                        null,
                        null,
                        null,
                        encrypt(requestPrivateKey(id),getRole(id).toString())));
                Packet p = receivePacket();
                if(p.getType() != RECEIVED_CONFIRM){
                    // Do something
                }
            }
        }
    }

    /**
     * Method that verifies that the client has the right role to perform an action
     *
     *
     * @param packet
     * @return boolean
     */
    private boolean verification(Packet packet){
        // TODO: Verify cert and the permission it grants
        if(packet.getCertificate()!= null){
            String decryptCert = decrypt(privateKey, packet.getCertificate());
            if(decryptCert.equals(clients.get(packet.getSenderID()).toString())){
                return true;
            }
            else {
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public void run() {
        running = true;
        LogMessage("Started client on thread: %s\n",Thread.currentThread().getName());
        messageHandler = new MessageHandler();
        actionHandler = new ActionHandler();

        Thread tm = new Thread(messageHandler,String.format("%s_MessageHandler",Thread.currentThread().getName()));
        Thread ta = new Thread(actionHandler,String.format("%s_ActionHandler",Thread.currentThread().getName()));

        tm.start();
        ta.start();

        while (running){
            // Threads are doing stuff
            try{
                Thread.sleep(TIMEOUT);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        //ending message handler and action handler
        messageHandler.end();
        actionHandler.end();

        // Waiting for the worker threads to die before shutting down the streams
        try{
            ta.join();
            tm.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        LogMessage("%s on: %s closing down!\n", name, Thread.currentThread().getName());
        socketClose();

        // Print all logged packets
        FileLogger.writeLogToFile(pktLog.getLoggedSequence());
        FileLogger.writeMessagesToFile(receivedMessages,name);
    }

    /**
     * Method that handles the actions, works in synchronization with checkmessages
     */
    private void handleActions() {
        Action action = null;
        try {
            action = this.actions.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(action == null)
            return;
        switch (action.getType()) {
            case "REGISTER":
                register(action.getSenderID(), Double.parseDouble(action.getMessageAsString()));
                break;
            case "ADD": {
                int success = add(action, Double.parseDouble(action.getMessageAsString()));
                if (success != 0) {
                    String error;
                    if (success == 1)
                        error = "ERROR: NOT ENOUGH CASH";
                    else if(success == 2)
                        error = "ERROR: USER DOES NOT HAVE ACCOUNT";
                    else
                        error = "ERROR: NO PERMISSION";
                    sendPacket(new Packet(Packet.PacketType.MSG,
                            name,
                            action.getSenderID(),
                            encrypt(requestPrivateKey(action.getSenderID()), String.format("%s -> %s\n", error, action.toString())),
                            null,
                            null,
                            null
                    ));
                    Packet p = receivePacket();
                    if (p.getType() != RECEIVED_CONFIRM) {
                        // Do something
                    }
                }
                break;
            }
            case "SUB": {
                int success = sub(action, Double.parseDouble(action.getMessageAsString()));
                if (success != 0) {
                    String error;
                    if (success == 1)
                        error = "ERROR: NOT ENOUGH CASH";
                    else if(success == 2)
                        error = "ERROR: USER DOES NOT HAVE ACCOUNT";
                    else
                        error = "ERROR: NO PERMISSION";
                    sendPacket(new Packet(Packet.PacketType.MSG,
                            name,
                            action.getSenderID(),
                            encrypt(requestPrivateKey(action.getSenderID()), String.format("%s -> %s\n", error, action.toString())),
                            null,
                            null,
                            null
                    ));
                    Packet p = receivePacket();
                    if (p.getType() != RECEIVED_CONFIRM) {
                        // Do something
                    }
                }
                break;
            }
            case "CHECK-IN": {
                boolean success = checkIn(action.getSenderID());
                if(!success){
                    sendPacket(new Packet(Packet.PacketType.MSG,
                            name,
                            action.getSenderID(),
                            encrypt(requestPrivateKey(action.getSenderID()), String.format("%s -> %s\n", "Can't check-in error: ", action.toString())),
                            null,
                            null,
                            null
                    ));
                    Packet p = receivePacket();
                    if (p.getType() != RECEIVED_CONFIRM) {
                        // Do something
                    }
                }
                break;
            }
            case "CHECK-OUT": {
                boolean success = checkOut(action.getSenderID());
                if(!success){
                    sendPacket(new Packet(Packet.PacketType.MSG,
                            name,
                            action.getSenderID(),
                            encrypt(requestPrivateKey(action.getSenderID()), String.format("%s -> %s\n", "Can't check-out error: ", action.toString())),
                            null,
                            null,
                            null
                    ));
                    Packet p = receivePacket();
                    if (p.getType() != RECEIVED_CONFIRM) {
                        // Do something
                    }
                }
                break;
            }
            case "NO PERMISSION":
                sendPacket(new Packet(Packet.PacketType.MSG,
                        name,
                        action.getSenderID(),
                        encrypt(requestPrivateKey(action.getSenderID()), String.format("%s -> %s\n", "ACCESS DENIED: ", action.toString())),
                        null,
                        null,
                        null
                ));
                Packet p = receivePacket();
                if (p.getType() != RECEIVED_CONFIRM) {
                    // Do something
                }
                break;
            default:
                LogMessage("ERROR: Unhandled action was sent to bank!\n %s\n",action.toString());
                break;
        }
    }

    private void socketClose() {
        try {
            if(!s.isInputShutdown() && !s.isOutputShutdown()) {
                objectOutputStream.writeObject(pktLog.newOut(
                        new Packet(Packet.PacketType.CLOSE,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null)));
                Packet p = pktLog.newIn(objectInputStream.readObject());
                if (p.getType() == Packet.PacketType.RECEIVED_CONFIRM) {
                    objectOutputStream.close();
                    objectInputStream.close();
                    s.close();
                }
            }
            else{
                s.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void shutdown(){
        running = false;
    }

    /**
     *
     * Method that will check for incoming messages from the server
     */
    private void checkMessages(){
        try{
            Packet p;
            synchronized (LOCK) {
                // Request messages from the server for this client
                objectOutputStream.writeObject(pktLog.newOut(
                        new Packet(Packet.PacketType.MSG_REQUEST,
                                name,
                                null,
                                null,
                                null,
                                null,
                                null)
                ));
                Packet conf = pktLog.newIn(objectInputStream.readObject());
                if (conf.getType() != RECEIVED_CONFIRM) {
                    // NO
                }
                // Response from server
                p = pktLog.newIn(objectInputStream.readObject());
                // Send confirmation of receiving the message
                objectOutputStream.writeObject(pktLog.newOut(new Packet(
                        RECEIVED_CONFIRM,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )));
            }
            if(p.getType() != Packet.PacketType.NO_MSGs) {
                Object o = p.getData();
                String t = new SimpleDateFormat("dd-MM-yyyy,HH:mm").format(new Date());
                if (o instanceof String) {
                    String message = decrypt(this.privateKey, (String) o);
                    parseAction(message, null);
                    message =  t + "-> " + message;
                    receivedMessages.add(message);
                    LogMessage("Message %s\n", message);

                } else if (o instanceof Message) {
                    String message = decrypt(this.privateKey, ((Message) o).getMessage());
                    receivedMessages.add(message);
                    parseAction(message, ((Message) o).getSender());
                    message = t + "-> " + message;
                    receivedMessages.add(message);
                    LogMessage("Message %s\n", message);

                } else if (o instanceof ArrayList) {
                    ArrayList<?> messages = (ArrayList<?>) o;
                    for (Object message : messages) {
                        Message m = (Message) message;
                        String decryptedMessage = decrypt(this.privateKey, m.getMessage());
                        parseAction(decryptedMessage, ((Message) message).getSender());
                        decryptedMessage = t + "-> " + decryptedMessage;
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
     * Gets actions from a client and makes them executable
     * @param action
     * @param sender
     */
    private void parseAction(String action, String sender){
        String[] parts = action.split("\\[");

        String actionType = parts[0];
        actionType = actionType.replace(" ", "");

        if(parts.length == 1){
            try{
                this.actions.put(new Action(actionType,sender,null,null,sender));
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        else if(parts.length == 2){
            String amount = parts[1];
            amount = amount.replace("]", "");

            try {
                this.actions.put(new Action(actionType, sender, null, amount,sender));
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        else if(parts.length == 3){
            String fromId = parts[1];
            fromId = fromId.replace("] ", "");

            if(!fromId.equals(sender) && !fromId.equals(name)){
                try {
                    this.actions.put(new Action("NO PERMISSION", sender, null, null));
                    return;
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            String amount = parts[2];
            amount = amount.replace("]", "");
            try {
                this.actions.put(new Action(actionType, fromId, null, amount,sender));
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        else if(parts.length == 4){
            String fromId = parts[1];
            fromId = fromId.replace("] ", "");

            if(!fromId.equals(sender) && !fromId.equals(name)){
                try {
                    this.actions.put(new Action("NO PERMISSION", sender, null, null));
                    return;
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            String toId = parts[2];
            toId = toId.replace("] ", "");

            String amount = parts[3];
            amount = amount.replace("]", "");
            try {
                this.actions.put(new Action(actionType,fromId,toId,amount,sender));
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Method that will transfer money from an account to another account.
     *
     * @param action
     * @param amount
     * @return integer depending on the error that happened, 0 in case of no error.
     */
    private int add(Action action, double amount){
        String fromAccount = action.getFromID();
        String toAccount = action.getToID();
        if(checkAccountExist(fromAccount) && checkAccountExist(toAccount)) {
            if (verification(fromAccount, action.getSenderID())) {
                if (amount <= balances.get(fromAccount)) {
                    double newBalanceSend = balances.get(fromAccount) - amount;
                    balances.replace(fromAccount, newBalanceSend);
                    double newBalanceRec = balances.get(toAccount) + amount;
                    balances.replace(toAccount, newBalanceRec);
                    return 0;
                } else {
                    LogMessage("not enough cash");
                    return 1;
                }
            }
            else{
                LogMessage("No permission");
                return 3;
            }
        }else {
            LogMessage("One of the two accounts does not exist");
            return 2;
        }
    }

    private boolean verification(String fromAccount, String senderID) {
        if(fromAccount.equals(senderID))
            return true;
        else return getRole(senderID) != Role.CUSTOMER && fromAccount.equals(name);
    }

    private int sub(Action action, double amount){
        String account = action.getFromID();
        if(checkAccountExist(account)) {
            if (verification(account, action.getSenderID())) {
                if (amount <= balances.get(account)) {
                    double newBalance = balances.get(account) - amount;
                    balances.replace(account, newBalance);
                    return 0;
                } else {
                    LogMessage("not enough cash");
                    return 1;
                }
            } else {
                LogMessage("No permission");
                return 3;
            }
        }
        else{
            LogMessage("account does not exist");
            return 2;
        }
    }

    /**
     * Method that checks if the account is registered in the bank
     *
     * @param account
     * @return
     */
    private boolean checkAccountExist(String account){
        if(balances.containsKey(account)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Method that will receive a packet
     *
     * @return packet
     */
    private Packet receivePacket(){
        synchronized (LOCK) {
            try {
                return pktLog.newIn(objectInputStream.readObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Method that will send a packet
     * @param p
     */
    private void sendPacket(Packet p){
        synchronized (LOCK) {
            try {
                objectOutputStream.writeObject(pktLog.newOut(p));
                Packet pIn = pktLog.newIn(objectInputStream.readObject());
                assert pIn.getType() == RECEIVED_CONFIRM;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    private String requestPrivateKey(String id){
        synchronized (LOCK) {
            sendPacket(pktLog.newOut(new Packet(Packet.PacketType.PUBLIC_KEY_REQUEST,
                    null,
                    id,
                    null,
                    null,
                    null,
                    null)));
            Packet pKeyPacket = receivePacket();
            try {
                if (pKeyPacket.getType() == Packet.PacketType.PUBLIC_KEY_REQUEST) {
                    objectOutputStream.writeObject(pktLog.newOut(new Packet(Packet.PacketType.RECEIVED_CONFIRM,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null)));
                } else {
                    // UNKNOWN USER ERROR
                    LogMessage("Trying to send message to unknown user!");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Objects.requireNonNull(pKeyPacket).getPublicKey();
        }
    }

    /**
     * Runs check message
     */
    private class MessageHandler implements Runnable{
        private boolean running;
        @Override
        public void run() {
            running = true;
            while(running) {
                checkMessages();
            }
            LogMessage("Close MessageHandler");
        }
        public void end(){
            running = false;
        }
    }

    /**
     * Runs handleActions
     */
    private class ActionHandler implements Runnable{
        private boolean running;
        @Override
        public void run() {
            running = true;
            while(running) {
                handleActions();
            }
            LogMessage("Close MessageHandler");
        }
        public void end(){
            running = false;
        }
    }
}

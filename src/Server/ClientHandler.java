package Server;

import Client.Client.ClientIDType;
import Shared.Packet;
import Shared.Packet.PacketType;
import Shared.PacketLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static Shared.Packet.PacketType.*;

/**
 * This class is instantiated for each new incoming connection and it is used to handle a client connected to this
 * socket
 *
 * @implements Runnable interface in order to created a thread from this class
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream OIS;
    private ObjectOutputStream OOS;
    private final PacketLogger packetLogger;
    private boolean active;

    public ClientHandler(Socket s) throws IOException {
        this.clientSocket = s;
        this.OOS = new ObjectOutputStream(s.getOutputStream());
        this.OIS = new ObjectInputStream(s.getInputStream());
        this.packetLogger = new PacketLogger();
        this.active = true;
    }

    /**
     * This method contains the loop that handles all traffic generated by the client
     * This method is called from Thread.start() in the server
     */
    @Override
    public void run() {
        System.out.println("Started client handler");
        try {
            System.out.println("Registering on server");
            register();
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
        while (active) {
            try {
                // TODO: For each packet received send confirmation to client
                Packet p = receiveFromClient();
                // Consider situation in which p = null?
                boolean success;
                assert p != null;
                PacketType type = p.getType();
                // Send confirmation of retrieval of the latest packet
                sendToClient(RECEIVED_CONFIRM);

                switch (type) {
                    case SYN_ACK:
                        System.out.println("Confirmation of successful registration with client received");
                        break;
                    case MSG_REQUEST: {
                        // Request all incoming messages
                        if (Server.isInDataBase(p.getSenderID(), ClientIDType.ID)) {
                            ArrayList<Message> messages = Server.checkMessages(p.getSenderID());
                            if (messages.size() > 0) {
                                success = sendMSG(messages);
                                if (!success) {
                                    System.out.println("Failed to send message or receive confirmation");
                                }
                            } else {
                                sendMSG(null);
                            }
                        } else {
                            System.out.println("Client is not registered!");
                        }
                    }
                    break;
                    case MSG: {
                        // Forward message
                        ClientIDType idType;
                        String idString;
                        if (p.getDestID() == null) {
                            idType = ClientIDType.NAME;
                            idString = p.getFullName();
                        } else {
                            idType = ClientIDType.ID;
                            idString = p.getDestID();
                        }
                        if (Server.isInDataBase(idString, idType)) {
                            success = Server.sendMessage(p);
                            if (!success) {
                                // TODO: Handle the situation in which an attempt was made to send a message to another
                                //  user through the server but it failed
                                System.out.println("Failed to send message or receive confirmation");
                            }
                        } else {
                            System.out.printf("User: %s is not in database\n",idString);
                            sendToClient(UNKNOWN_USER_ERROR);
                            System.out.println("Client defined by destination is not registered!");
                        }
                    }
                    break;
                    case PUBLIC_KEY_REQUEST:
                        // Since the public key is being shared here one does not have to encrypt this public key with
                        // the public key of the recipient
                        if (p.getDestID() != null) {
                            String pKey = Server.getPublicKeyByID(p.getDestID());
                            success = sendToClient(new Packet(
                                    PacketType.PUBLIC_KEY_REQUEST,
                                    p.getSenderID(),
                                    p.getDestID(),
                                    null,
                                    null,
                                    null,
                                    pKey
                            ));
                        }
                        else if(p.hasName()){
                            String pKey = Server.getPublicKeyByName(p.getFullName());
                            success = sendToClient(new Packet(
                                    PacketType.PUBLIC_KEY_REQUEST,
                                    p.getSenderID(),
                                    p.getDestID(),
                                    null,
                                    null,
                                    null,
                                    pKey
                            ));
                        }
                        else{
                            // Send an error packet if the destination is undefined
                            sendToClient(ERROR);
                        }
                        break;
                    case CLOSE:
                        System.out.println("Close requested!");
                        active = false;
                        break;
                    default:
                        // TODO: Implement some error resolving method for this situation
                        //  (Also check whether it will every get here)
                        break;
                }

            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sends all messages for the client this process in handling to them
     *
     * @param messages This array includes all unread messages
     * @return Returns true if message was successfully sent
     */
    private boolean sendMSG(ArrayList<Message> messages) {
        if(messages != null) {
            try {
                // Send array of messages in one packet to the client and wait for confirmation of receiving
                // TODO: Encrypt the data in the messages
                OOS.writeObject(packetLogger.newOut(new Packet(
                                MSG,
                                null,
                                null,
                                messages,
                                null,
                                null,
                                null
                        )
                ));
                Packet conf = packetLogger.newIn(OIS.readObject());
                return conf.getType() == RECEIVED_CONFIRM;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                OOS.writeObject(packetLogger.newOut(new Packet(
                                NO_MSGs,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ));
                Packet conf = packetLogger.newIn(OIS.readObject());
                return conf.getType() == RECEIVED_CONFIRM;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean sendToClient(PacketType packetType){
        try{
            OOS.writeObject(packetLogger.newOut(new Packet(
                    packetType,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)));
            if(packetType != RECEIVED_CONFIRM) {
                Packet packetIn = packetLogger.newIn(OIS.readObject());
                if (packetIn.getType() == RECEIVED_CONFIRM) {
                    return true;
                }
            }
            else{
                return true;
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean sendToClient(Packet packet){
        try{
            OOS.writeObject(packetLogger.newOut(packet));
            Packet packetIn = packetLogger.newIn(OIS.readObject());
            if(packetIn.getType() == RECEIVED_CONFIRM){
                return true;
            }
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    private Packet receiveFromClient() {
        try {
            return packetLogger.newIn(OIS.readObject());
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method takes care of the registration with the client
     *
     * @throws IOException            Possible exception
     * @throws ClassNotFoundException Possible exception
     */
    private void register() throws IOException, ClassNotFoundException {
        Packet p = receiveFromClient();
        assert p != null; // TODO: Find an alternative for this
        if (p.getType() == SYN) {
            boolean req = Server.register(p); // You need the public static access here
            if (req) {
                // Upon registration send acknowledgement of successful registration
                OOS.writeObject(this.packetLogger.newOut(
                        new Packet(
                                ACK,
                                null,
                                p.getSenderID(),
                                null,
                                null,
                                null,
                                null
                        )
                ));
            }
        }
    }

    /**
     * Closes all streams
     *
     * @throws IOException Closing streams can cause an IOException
     */
    private void close() throws IOException {
        OOS.close();
        OIS.close();
        clientSocket.close();
    }

    /**
     * Stops the execution of this process
     */
    public void kill() {
        this.active = false;
    }

    /**
     * Check if this process is still running
     *
     * @return true if process is active
     */
    public boolean isActive() {
        return this.active;
    }

}
package Server;

import Shared.Packet;
import Shared.Packet.PacketType;
import Shared.PacketLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static Shared.Packet.PacketType.ACK;
import static Shared.Packet.PacketType.SYN;

/**
 * This class is instantiated for each new incoming connection and it is used to handle a client connected to this
 * socket
 *
 * @implements Runnable interface in order to created a thread from this class
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ObjectInputStream OIS;
    private final ObjectOutputStream OOS;
    private final PacketLogger packetLogger;
    private boolean active;

    public ClientHandler(Socket s) throws IOException {
        this.clientSocket = s;
        this.OIS = new ObjectInputStream(s.getInputStream());
        this.OOS = new ObjectOutputStream(s.getOutputStream());
        this.packetLogger = new PacketLogger();
        this.active = true;
    }

    /**
     * This method contains the loop that handles all traffic generated by the client
     * This method is called from Thread.start() in the server
     */
    @Override
    public void run() {
        while (active) {
            try {
                Packet p = this.packetLogger.newIn(OIS.readObject());
                PacketType type = p.getType();
                switch (type) {
                    case SYN_ACK:
                        System.out.println("Confirmation of successful registration with client received");
                        break;
                    case MSG_REQUEST:
                        // Request all incoming messages
                        ArrayList<Message> messages = Server.checkMessages(p.getSenderID());
                        sendMSGToClient(messages);
                        break;
                    case MSG:
                        // Forward message
                        boolean success = Server.sendMessage(p);
                        if (!success) {
                            // TODO: Handle the situation in which an attempt was made to send a message to another
                            //  user through the server but it failed
                        }
                        break;
                    default:
                        // TODO: Implement some error resolving method for this situation
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
     */
    private void sendMSGToClient(ArrayList<Message> messages) {
        // TODO: Decide on either send message count and then all messages in different packets or
        //  send everything by using serialisation
    }

    /**
     * This method takes care of the registration with the client
     *
     * @throws IOException            Possible exception
     * @throws ClassNotFoundException Possible exception
     */
    protected void register() throws IOException, ClassNotFoundException {
        Packet p = this.packetLogger.newIn(OIS.readObject());
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
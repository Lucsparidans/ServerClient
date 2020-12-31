package Client;

import Shared.FileLogger;
import Shared.Packet;
import Shared.PacketLogger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static Shared.ConsoleLogger.LogMessage;

public class Organisation implements Runnable{

    private static final boolean DEBUG = false;

    private final PacketLogger pktLog;

    private final ArrayList<String> receivedMessages;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private Socket s;

    private boolean running;
    private static final int PORT = 4444;
    private static String NAME;
    private String duration;


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
        };

    }
    private final HashMap<String, Role> employees = new HashMap<>();
    //TODO: ADD GOOD VERIFICATION
    private final HashMap<String, Double> balances = new HashMap<>();

    public Organisation(String file) {
        // TODO: GET ROLES FROM JSON
        pktLog = new PacketLogger();
        s = null;
        receivedMessages = new ArrayList<>();
        try {
            parseJSON(file); //TODO: Parse the JSON file and extract fields
            s = connectToServer(InetAddress.getLoopbackAddress(),PORT);
            register();
            verification();
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
//                objectOutputStream.writeObject(pktLog.newOut(
//                        new Packet(Packet.PacketType.SYN,
//                                id,
//                                null,
//                                null,
//                                name.split(" ")[0].toUpperCase(),
//                                name.split(" ")[1].toUpperCase(),
//                                publicKey)));
                Packet p = pktLog.newIn(objectInputStream.readObject());
                if (p != null) {
                    if (p.getType() == Packet.PacketType.ACK) {
                        verified = true;
//                        objectOutputStream.writeObject(pktLog.newOut(
//                                new Packet(
//                                        Packet.PacketType.SYN_ACK,
//                                        id,
//                                        null,
//                                        null,
//                                        name.split(" ")[0].toUpperCase(),
//                                        name.split(" ")[1].toUpperCase(),
//                                        publicKey
//                                )));
                    }
                    p = pktLog.newIn(objectInputStream.readObject());
                    if(p.getType() == Packet.PacketType.RECEIVED_CONFIRM) System.out.println("ACK");
                } else {
                    //Thread.sleep(Integer.parseInt(timeout) * 1000L);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void parseJSON(String file) {
        this.NAME = "BANK";
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

    private void register(){
        // TODO: IO and exchange certs
    }

    private void verification(){
        // TODO: Verify cert and the permission it grants
    }

    @Override
    public void run() {

        while (running){
            // TODO: WAIT FOR MESSAGE AND EXECUTE ACTION IN THE MESSAGE
            LogMessage("Started client on thread: %s\n",Thread.currentThread().getName());
            long endTime;
            if(DEBUG){
                endTime = System.currentTimeMillis() + Long.parseLong(this.duration);
            }
            else{
                endTime = System.currentTimeMillis() + Long.parseLong(this.duration) * 1000L;
            }
            while(System.currentTimeMillis() < endTime){
                // TODO: Consider the following: What is we made this method multithreaded as well in the sense that we
                //  would at all times have one thread checking for messages and the other executing the actions that
                //  the first extracted from the incoming messages.

                checkMessages();
                handleTransactions();
            }

            LogMessage("Client on: %s closing down!\n  Cause: End of lifetime reached.\n", Thread.currentThread().getName());
            socketClose();

            // Print all logged packets
            FileLogger.writeLogToFile(pktLog.getLoggedSequence());
            FileLogger.writeMessagesToFile(receivedMessages,NAME);
        }
    }

    private void handleTransactions() {
        // TODO: Handle list of pending transactions
    }

    private void socketClose() {
        // TODO: Handle end of lifetime for this client or for the server
        //  Socket and streams need to closed as well (prevent memory leaks)
    }

    private void checkMessages() {
        // TODO: Ask the server whether there are any incoming messages stored for this organisation
    }


    private void add(String id, String fromAccount, String toAccount, double amount){
        if(amount>=balances.get(fromAccount)){
            double newBalanceSend = balances.get(fromAccount) - amount;
            balances.replace(fromAccount, newBalanceSend);
            double newBalanceRec = balances.get(toAccount) + amount;
            balances.replace(toAccount, newBalanceRec);
        }
        else{
            System.out.println("not enough cash");
        }
    }

    private void sub(String id, String account, double amount){
        if(amount>=balances.get(account)){
            double newBalance = balances.get(account) - amount;
            balances.replace(account, newBalance);
        }
        else{
            System.out.println("not enough cash");
        }
    }

    private Packet receivePacket(){
        try {
            return pktLog.newIn(objectInputStream.readObject());
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }
    private void sendPacket(Packet p){
        try {
            objectOutputStream.writeObject(pktLog.newOut(p));
            Packet pIn = pktLog.newIn(objectInputStream.readObject());
            assert pIn.getType() == Packet.PacketType.RECEIVED_CONFIRM;
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}

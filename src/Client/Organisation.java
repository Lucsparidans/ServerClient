package Client;

import Server.ClientInfo;
import Shared.Packet;
import Shared.PacketLogger;

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

    public enum Role{
        //TODO: ADD ALL ROLES
    }

    private final HashMap<String, Role> employees = new HashMap<>();
    //TODO: ADD GOOD VERIFICATION
    private final HashMap<String, Double> balances = new HashMap<>();

    public Organisation(String file) {
        // TODO: GET ROLES FROM JSON
        pktLog = new PacketLogger();
        receivedMessages = new ArrayList<>();
    }

    @Override
    public void run() {
        // TODO: WAIT FOR MESSAGE AND EXECUTE ACTION IN THE MESSAGE
    }

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


    private void register(){

    }

    private void checkMessage(){

    }

    private void verification(){

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
        else{ System.out.println("not enough cash");
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

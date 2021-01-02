package Server;

import Client.Client.ClientIDType;
import Client.Organisation;
import Shared.Message;
import Shared.Packet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static Shared.ConsoleLogger.LogMessage;

public class Server {

    // TODO: Add locked synchronisation for data structure

    public static final int PORT = 4444;
    public static final InetAddress INET_ADDRESS = InetAddress.getLoopbackAddress();
    private static final int BACK_LOG = 5;

    private static final int CLIENT_LIMIT = 10;
    private static final ArrayList<ClientHandler> SESSIONS = new ArrayList<>();
    private static final HashMap<ClientHandler, Thread> THREAD_BY_CLIENT = new HashMap<>();
    private static final HashMap<String, ArrayList<Message>> MSG_BY_ID = new HashMap<>();
    private static final HashMap<String, ClientInfo> INFO_BY_ID = new HashMap<>();
    private static final HashMap<String, String> NAME_TO_ID = new HashMap<>();
    private static final ArrayList<String> ID_LIST = new ArrayList<>();
    private static final Object LOCK = new Object();

    private static final String ORG_PATH = "src/JSON_files/organizations.json";
    private static final ArrayList<Organisation> ORGS = new ArrayList<>();

    public static void main(String[] args) {
        // TODO: close socket when client disconnects
        LogMessage(INET_ADDRESS);
        try {
            ServerSocket ss = new ServerSocket(PORT,BACK_LOG,INET_ADDRESS);
            new Thread(new InitOrgs()).start();
            while (!ss.isClosed()) {
                // TODO: create method to shutdown (all) threads
                if(SESSIONS.size() < CLIENT_LIMIT){
                    Socket socket = ss.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    SESSIONS.add(clientHandler);
                    Thread thread = new Thread(clientHandler);
                    THREAD_BY_CLIENT.put(clientHandler,thread);
                    thread.start();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        ORGS.forEach(Organisation::shutdown);
    }

    /**
     * Method that takes care of adding a new user to the database (and confirms authenticity?)
     *
     * @param packetIn Packet including the SYN request
     * @return true when client successfully registered, otherwise false
     */
    public static boolean register(Packet packetIn) {
        synchronized (LOCK) {
            if (ID_LIST.contains(packetIn.getSenderID())) {
                // User is already registered
                return false;
            } else {
                // Add user to the database structures (multiple formats for higher performance on specific queries)
                INFO_BY_ID.put(packetIn.getSenderID(), new ClientInfo(
                        packetIn.getSenderID(),
                        packetIn.getFirstName(),
                        packetIn.getLastName(),
                        packetIn.getPublicKey()));
                NAME_TO_ID.put(packetIn.getFullName(), packetIn.getSenderID());
                ID_LIST.add(packetIn.getSenderID());
                MSG_BY_ID.put(packetIn.getSenderID(), new ArrayList<>());
                return true;
            }
        }
    }

    public static void close(ClientHandler clientHandler) {
        SESSIONS.remove(clientHandler);
        THREAD_BY_CLIENT.remove(clientHandler);
    }

    public static ArrayList<Message> checkMessages(String id){
        synchronized (LOCK) {
            if(MSG_BY_ID.get(id).size() > 0) {
                ArrayList<Message> m = MSG_BY_ID.get(id);
                ArrayList<Message> messages = new ArrayList<>();
                m.forEach((message -> messages.add(message.clone())));
                MSG_BY_ID.get(id).clear();
                return messages;
            }
            return MSG_BY_ID.get(id);
        }
    }

    public static boolean sendMessage(Packet pkt) {
        synchronized (LOCK) {
            // TODO: Check whether the sender is registered??
            if (pkt.getDestID() != null) {
                if (ID_LIST.contains(pkt.getDestID())) {
                    // ID is registered
                    Object data = pkt.getData();
                    if(data instanceof String){
                        MSG_BY_ID.get(pkt.getDestID()).add(new Message(
                                pkt.getSenderID(),
                                pkt.getDestID(),
                                (String) pkt.getData()));
                    }
                    else if(data instanceof Message){
                        MSG_BY_ID.get(pkt.getDestID()).add(new Message(
                                pkt.getSenderID(),
                                pkt.getDestID(),
                                ((Message) pkt.getData()).getMessage()));
                    }
                    else if(data instanceof ArrayList){
                        ArrayList<?> messages = (ArrayList<?>) pkt.getData();
                        for (Object message : messages) {
                            MSG_BY_ID.get(pkt.getDestID()).add(new Message(
                                    pkt.getSenderID(),
                                    pkt.getDestID(),
                                    ((Message) message).getMessage()));
                        }
                    }
                    // Message successfully sent
                    return true;
                } else {
                    // ID is not registered
                    // Cannot send message
                    return false;
                }
            } else if (pkt.hasName()) {
                String fullName = pkt.getFullName();
                String ID = NAME_TO_ID.get(fullName);
                if (ID != null) {
                    // ID was found in the database
                    Object data = pkt.getData();
                    if(data instanceof String){
                        MSG_BY_ID.get(ID).add(new Message(
                                pkt.getSenderID(),
                                ID,
                                (String) data));
                    }else if(data instanceof Message){
                        MSG_BY_ID.get(ID).add(new Message(
                                pkt.getSenderID(),
                                ID,
                                ((Message) data).getMessage()));
                    }else if(data instanceof ArrayList) {
                        ArrayList<?> messages = (ArrayList<?>) data;
                        for (Object message : messages) {
                            MSG_BY_ID.get(ID).add(new Message(
                                    pkt.getSenderID(),
                                    ID,
                                    ((Message) message).getMessage()));
                        }
                    }
                    // Message(s) successfully sent
                    return true;
                } else {
                    // ID was not found in the database
                    // Cannot send the message
                    return false;
                }
            } else {
                // Something went wrong!
                // TODO: Handle this without throwing an exception
                throw new IllegalStateException("Sending packet without destination!");
            }
        }
    }
    private static void initializeOrganisations(){
        JSONParser jsonParser = new JSONParser();
        try{
            JSONObject data = (JSONObject) jsonParser.parse(new FileReader(ORG_PATH));
            JSONArray Organisations = (JSONArray) data.get("Organizations");
            for (Object object :
                    Organisations) {
                JSONObject org = (JSONObject) object;
                Organisation organisation = new Organisation(org);
                ORGS.add(organisation);
                new Thread(organisation).start();
            }
            System.out.println();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static class InitOrgs implements Runnable{
        @Override
        public void run() {
            initializeOrganisations();
        }
    }
    public static boolean isInDataBase(String data, ClientIDType type){
        synchronized (LOCK) {
            if (type == ClientIDType.NAME) {
                return NAME_TO_ID.containsKey(data);
            }
            return ID_LIST.contains(data);
        }
    }
    public static String getPublicKeyByID(String id){
        synchronized (LOCK) {
            return INFO_BY_ID.get(id).getPublicKey();
        }
    }
    public static String getPublicKeyByName(String name) {
        synchronized (LOCK) {
            String id = NAME_TO_ID.get(name);
            return INFO_BY_ID.get(id).getPublicKey();
        }
    }

    public static String nameToID(String fullName) {
        synchronized (LOCK) {
            return NAME_TO_ID.get(fullName);
        }
    }
}
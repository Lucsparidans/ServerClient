package Server;

import Shared.Packet;
import Shared.Packet.DataFormat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final ArrayList<ClientHandler> SESSIONS = new ArrayList<>();
    private static final HashMap<String, ArrayList<Message>> MSG_BY_ID = new HashMap<>();
    private static final HashMap<String, ClientInfo> INFO_BY_ID = new HashMap<>();
    private static final HashMap<String, String> NAME_TO_ID = new HashMap<>();
    private static final ArrayList<String> ID_LIST = new ArrayList<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(4999);
        while (!ss.isClosed()) {
            // TODO: Accept all incoming connections while there is capacity for them
            // TODO: Handle all accepted connection on a worker thread (Master-worker architecture)
            // TODO: Handle all generated threads
            // TODO: Collect all threads in some data structure
            // TODO: create method to shutdown (all) threads
            // TODO: Add synchronisation to prevent concurrent access exceptions
            Socket s = ss.accept();
            ClientHandler clientHandler = new ClientHandler(s);
            clientHandler.register();
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    }

    /**
     * Method that takes care of adding a new user to the database (and confirms authenticity?)
     *
     * @param packetIn Packet including the SYN request
     * @return A string with feedback TODO: Change this
     */
    public synchronized static boolean register(Packet packetIn) {
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
            return true;
        }
    }

    public static synchronized ArrayList<Message> checkMessages(String id) throws IOException, ClassNotFoundException {
        return MSG_BY_ID.get(id);
        // region OLD CODE
//        // CHECK FOR MESSAGES
//        String clientId = in.getSenderID();
//        ArrayList<String> myMessages = getMyMessages(clientId);
//        int messagesNumber = myMessages.size();
//
//        if (messagesNumber > 0) {
//            // Send messages number to Client
//            OOS.writeObject(pktLog.newOut(     // Sends the number of messages it will send
//                    new Packet(in.getType(),
//                            in.getSenderID(),
//                            in.getDestID(),
//                            Integer.toString(messagesNumber),
//                            null,
//                            null,
//                            null)
//            ));
//            for (String mes : myMessages) {
//                // Send messages to client
//                OOS.writeObject(pktLog.newOut(     // Sends the number of messages it will send
//                        new Packet(in.getType(),
//                                in.getSenderID(),
//                                in.getDestID(),
//                                mes,
//                                null,
//                                null,
//                                null)
//                ));
//                System.out.println(mes);
//            }
//        } else {
//            // Send to Client that are no messages
//            OOS.writeObject(pktLog.newOut(     // Sends the number of messages it will send
//                    new Packet(PacketType.NO_MSGs,
//                            in.getSenderID(),
//                            in.getDestID(),
//                            "No available messages",
//                            null,
//                            null,
//                            null)
//            ));
//        }
        // endregion
    }

    public synchronized static boolean sendMessage(Packet pkt) {
        // TODO: Check whether the sender is registered??
        if (pkt.getDestID() != null) {
            if (ID_LIST.contains(pkt.getDestID())) {
                // ID is registered
                DataFormat dataFormat = pkt.getDataFormat();
                switch (dataFormat){
                    case STRING:
                        MSG_BY_ID.get(pkt.getDestID()).add(new Message(
                                pkt.getSenderID(),
                                pkt.getDestID(),
                                (String)pkt.getData()));
                        break;
                    case MESSAGE:
                        MSG_BY_ID.get(pkt.getDestID()).add(new Message(
                                pkt.getSenderID(),
                                pkt.getDestID(),
                                ((Message) pkt.getData()).getMessage()));
                        break;
                    case ARRAYLIST_MESSAGES:
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
                DataFormat dataFormat = pkt.getDataFormat();
                switch (dataFormat) {
                    case STRING:
                        MSG_BY_ID.get(ID).add(new Message(
                                pkt.getSenderID(),
                                ID,
                                (String) pkt.getData()));
                        break;
                    case MESSAGE:
                        MSG_BY_ID.get(ID).add(new Message(
                                pkt.getSenderID(),
                                ID,
                                ((Message) pkt.getData()).getMessage()));
                        break;
                    case ARRAYLIST_MESSAGES:
                        ArrayList<?> messages = (ArrayList<?>) pkt.getData();
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



///// OLD SERVER CLASS IF YOU WANT TO RUN


/*package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private static final ArrayList<ClientInfo> db = new ArrayList<>();
    private static final ArrayList<Message> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(4999);

        // Test
        Message m1 = new Message("abc", "abc", "Hey!");
        messages.add(m1);
        Message m2 = new Message("abc", "abc", "How are you bro?");
        messages.add(m2);
        Message m3 = new Message("abc", "abc", "What are you doing?");
        messages.add(m3);


        //TODO: Provide exit clause for this loop
        while (true) {
            System.out.println("Waiting for a client..");
            // Accept Client
            Socket s = ss.accept();
            System.out.println("Client Connected");

            PrintWriter pr = new PrintWriter(s.getOutputStream());

            // Read message from client
            InputStreamReader in = new InputStreamReader(s.getInputStream());
            BufferedReader bf = new BufferedReader(in);

            int type = Integer.parseInt(bf.readLine());
            System.out.println("Request of type : " + type);

            if (type == (1)) {
                // REGISTRATION
                String resp = registration(bf);

                // Send message to Client
                pr.println(resp);
                pr.flush();

            } else if (type == (2)) {
                // CHECK FOR MESSAGES
                checkMessages(bf, pr);

            } else if (type == 3) {
                // SEND MESSAGE WITH ID
                String resp = sendMessage(bf, false);

                // Send message to Client
                pr.println(resp);
                pr.flush();

            } else if (type == 4) {
                // SEND MESSAGE WITH NAME
                String resp = sendMessage(bf, true);

                System.out.println(resp);
                // Send message to Client
                pr.println(resp);
                pr.flush();
            } else {

                // Send message to Client
                pr.println("Error No action recognized");
                pr.flush();
            }
        }
    }

    public static String registration(BufferedReader bf) throws IOException {

        String id = bf.readLine();
        String firstName = bf.readLine();
        String lastName = bf.readLine();
        String publicKey = bf.readLine();

        for (ClientInfo c: db) {
            if (c.getId().equals(id)) return "Client has been already registered";
        }

        ClientInfo c = new ClientInfo(id, firstName, lastName, publicKey);
        db.add(c);

        return "Successful registration";
    }

    public static void checkMessages(BufferedReader bf, PrintWriter pr) throws IOException {
        // CHECK FOR MESSAGES
        String clientId = bf.readLine();
        ArrayList<String> myMessages = getMyMessages(clientId);
        int messagesNumber = myMessages.size();

        if (messagesNumber > 0) {
            // Send messages number to Client
            pr.println(messagesNumber);

            for (String mes : myMessages) {
                // Send messages to client
                pr.println(mes);

                System.out.println(mes);

                pr.flush();
            }
        } else {
            // Send to Client that are no messages
            pr.println("500");
            pr.flush();
        }
    }

    public static String sendMessage(BufferedReader bf, boolean nameId) throws IOException {

        if (nameId) {
            String fromId = bf.readLine();

            String firstName = bf.readLine();
            String lastName = bf.readLine();
            String toId = getIdByName(firstName, lastName);

            String message = bf.readLine();

            if (!toId.equals("-1")) {
                messages.add(new Message(fromId, toId, message));

                return "Success message sent";
            }
            return "User not found";
        }


        String fromId = bf.readLine();
        String toId = bf.readLine();
        String message = bf.readLine();

        messages.add(new Message(fromId, toId, message));

        return "Success message sent";
    }

    public static ArrayList<String> getMyMessages(String id) {
        ArrayList<String> myMessages = new ArrayList<>();

        for (Message mes : messages) {
            if (mes.getToId().equals(id)) {
                myMessages.add(mes.getMessage());
            }
        }
        return myMessages;
    }

    public static String getKeyById(String id) {
        for (ClientInfo c : db) {
            if (c.getId().equals(id)) {
                return c.getPublicKey();
            }
        }
        return "Error";
    }

    public  static String getIdByName(String firstName, String lastName) {
        for (ClientInfo c: db) {
            if (c.getFirstName().equals(firstName) && c.getLastName().equals(lastName)) return c.getId();
        }
        return "-1";
    }



}
*/
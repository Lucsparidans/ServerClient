package Server;

import Shared.Packet;
import Shared.PacketLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final ArrayList<ClientInfo> db = new ArrayList<>();
    private static final ArrayList<Message> messages = new ArrayList<>();
    private static final ArrayList<PacketLogger> pktLoggers = new ArrayList<>();
    private static final HashMap<Socket,PacketLogger> sockLoggers = new HashMap<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(4999);

        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        // Test
        Message m1 = new Message("abc", "abc", "Hey!");
        messages.add(m1);
        Message m2 = new Message("abc", "abc", "How are you bro?");
        messages.add(m2);
        Message m3 = new Message("abc", "abc", "What are you doing?");
        messages.add(m3);

        while (true) {
            //TODO: Add exit condition
            System.out.println("Waiting for a client..");
            // Accept Client
            Socket s = ss.accept();
            PacketLogger pktLog = new PacketLogger();
            pktLoggers.add(pktLog);
            sockLoggers.put(s,pktLog);
            System.out.println("Client Connected");

            outputStream = new ObjectOutputStream(s.getOutputStream());
            inputStream = new ObjectInputStream(s.getInputStream());

            // Read message from client
            pktLog.newIn(inputStream.readObject());

            Packet lastIn = pktLog.getLastIn();

            System.out.printf("Request of type : %d\n", lastIn.getType());

            if (pktLog.getLastIn().getType() == (1)) {
                // REGISTRATION
                String resp = registration(pktLog.getLastIn());

                // Send message to Client
                outputStream.writeObject(pktLog.newOut(
                        new Packet(1,
                                "Server",
                                lastIn.getSenderID(),
                                resp,
                                null,
                                null,
                                null)
                ));
            } else if (pktLog.getLastIn().getType() == (2)) {
                // CHECK FOR MESSAGES

                checkMessages(pktLog,inputStream,outputStream);

            } else if (lastIn.getType() == 3) {
                // SEND MESSAGE WITH ID
                String resp = sendMessage(lastIn, false);

                // Send message to Client
                outputStream.writeObject(pktLog.newOut(     // Forwards message
                        new Packet(3,
                                "Server",
                                lastIn.getSenderID(),
                                resp,
                                null,
                                null,
                                null)
                ));

            } else if (lastIn.getType() == 4) {
                // SEND MESSAGE WITH NAME
                String resp = sendMessage(lastIn, true);

                System.out.println(resp);
                // Send message to Client
                outputStream.writeObject(pktLog.newOut(     // Forwards message
                        new Packet(4,
                                "Server",
                                lastIn.getSenderID(),
                                resp,
                                null,
                                null,
                                null)
                ));
            } else {
                // Send message to Client
                outputStream.writeObject(pktLog.newOut(
                        new Packet(-1,
                                lastIn.getSenderID(),
                                lastIn.getDestID(),
                                "ERROR: Something went wrong",
                                null,
                                null,
                                null)
                ));
            }
        }
    }

    public static String registration(Packet packetIn) {

        for (ClientInfo c: db) {
            if (c.getId().equals(packetIn.getSenderID())) return "Client has been already registered";
        }

        ClientInfo c = new ClientInfo(packetIn.getSenderID(), packetIn.getFirstName(), packetIn.getLastName(), packetIn.getPublicKey());
        db.add(c);

        return "Successful registration";
    }

    public static void checkMessages(PacketLogger pktLog, ObjectInputStream OIS, ObjectOutputStream OOS) throws IOException, ClassNotFoundException {
        // CHECK FOR MESSAGES
        Packet in = pktLog.newIn(OIS.readObject());
        String clientId = in.getSenderID();
        ArrayList<String> myMessages = getMyMessages(clientId);
        int messagesNumber = myMessages.size();

        if (messagesNumber > 0) {
            // Send messages number to Client
            OOS.writeObject(pktLog.newOut(     // Sends the number of messages it will send
                    new Packet(in.getType(),
                            in.getSenderID(),
                            in.getDestID(),
                            Integer.toString(messagesNumber),
                            null,
                            null,
                            null)
            ));
            for (String mes : myMessages) {
                // Send messages to client
                OOS.writeObject(pktLog.newOut(     // Sends the number of messages it will send
                        new Packet(in.getType(),
                                in.getSenderID(),
                                in.getDestID(),
                                mes,
                                null,
                                null,
                                null)
                ));
                System.out.println(mes);
            }
        } else {
            // Send to Client that are no messages
            OOS.writeObject(pktLog.newOut(     // Sends the number of messages it will send
                    new Packet(500,
                            in.getSenderID(),
                            in.getDestID(),
                            "No available messages",
                            null,
                            null,
                            null)
            ));
        }
    }

    public static String sendMessage(Packet pkt, boolean nameId) {

        if (nameId) {
            String toId = getIdByName(pkt.getFirstName(), pkt.getLastName());
            if (!toId.equals("-1")) {
                messages.add(new Message(pkt.getSenderID(), toId, pkt.getData()));
                return "Success message sent";
            }
            return "User not found";
        }
        messages.add(new Message(pkt.getSenderID(), pkt.getDestID(), pkt.getData()));
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
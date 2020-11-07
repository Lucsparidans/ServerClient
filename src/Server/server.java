package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class server {

    private static ArrayList<ClientInfo> db =new ArrayList();
    private static ArrayList<Message> messages = new ArrayList();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(4999);

        // Test
        Message m1 = new Message("abc", "abc", "Hey!");
        messages.add(m1);
        Message m2 = new Message("abc", "abc", "How are you bro?");
        messages.add(m2);
        Message m3 = new Message("abc", "abc", "What are you doing?");
        messages.add(m3);


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

                String id = bf.readLine();
                String firstName = bf.readLine();
                String lastName = bf.readLine();
                String publicKey = bf.readLine();
                registration(id, firstName, lastName, publicKey);

                // Send message to Client
                pr.println("Success");
                pr.flush();

            } else if (type == (2)) {
                // CHECK FOR MESSAGES
                String clientId = bf.readLine();
                ArrayList<String> myMessages = getMyMessages(clientId);
                int messagesNumber = myMessages.size();

                if (messagesNumber > 0) {
                    // Send messages number to Client
                    pr.println(messagesNumber);

                    for (String mes: myMessages) {
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

            } else if (type == 3) {
                // Send message
                String fromId = bf.readLine();
                String toId = bf.readLine();
                String message = bf.readLine();

                sendMessage(fromId, toId, message);
            } else {
                // Send message to Client
                pr.println("Error No action recognized");
                pr.flush();
            }
        }
    }

    public static void registration(String id, String firstName, String lastName, String publicKey ) {
        ClientInfo c = new ClientInfo(id, firstName, lastName, publicKey);
        db.add(c);
    }

    public static ArrayList<String> getMyMessages(String id) {
        ArrayList myMessages = new ArrayList();

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

    public static void sendMessage(String fromId, String toId, String message) {
        messages.add(new Message(fromId, toId, message));
    }


}

package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class client {

    private static Socket s;
    private static PrintWriter pr;

    public static void main(String[] args) throws IOException {
        s = new Socket("localhost", 4999);
        pr = new PrintWriter(s.getOutputStream());


        // Constantly checking for new messages
        while (true) {

            // Send get mesages request to server
            pr.println(2);
            pr.println("abc");

            // Send message
            pr.flush();

            try {
                // Read message from Server
                InputStreamReader in = new InputStreamReader(s.getInputStream());
                BufferedReader bf = new BufferedReader(in);

                int messagesNumber = Integer.parseInt(bf.readLine());

                if (messagesNumber != 500) {
                    for (int i = 0; i < messagesNumber; i++) {
                        String message = bf.readLine();
                        System.out.println("Message " + i + " : " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }


//        while (true) {
//            System.out.println("Please enter the action");
//            // Enter data using BufferReader
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//            System.out.println("Please enter the action");
//
//
//            // Reading data using readLine
//            String action = "SEND";
//
//            if (action.equals("SEND")) {
//                System.out.println("Who you want to send the message to?");
//                String id = "abc";
//
//                System.out.println("Please enter the message");
//                String message = "hey broooo";
//
//
//                // Write message
//                pr.println("3");
//                pr.println("abc");
//                pr.println(id);
//                pr.println(message);
//                // Send message
//                pr.flush();
//            }
//
//        }


    }

    public static void sendString(String str) {


    }
}

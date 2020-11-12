package Client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Client {

    private Socket s;
    private PrintWriter pr;

    // Client Information
    // general
    private String duration;
    private String retries;
    private String timeout;

    // person
    private String id;
    private String name;
    private String privateKey;
    private String publicKey;

    // server
    private String ip;
    private String port;

    // actions
    private JSONArray actions;

    public Client(String file) {

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(file));

            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            JSONObject jsonObject = (JSONObject) obj;

            // A JSON array. JSONObject supports java.util.List interface.
            JSONObject general = (JSONObject) jsonObject.get("general");
            this.duration = (String) general.get("duration");
            this.retries = (String) general.get("retries");
            this.timeout = (String) general.get("timeout");

            JSONObject person = (JSONObject) jsonObject.get("person");
            this.id =  (String) person.get("id");;
            this.name = (String) person.get("name");

            System.out.println(id + name);

            JSONObject keys = (JSONObject) person.get("keys");
            this.privateKey = (String) keys.get("private");
            this.publicKey = (String) keys.get("public");

            JSONObject server = (JSONObject) jsonObject.get("server");
            this.ip = (String) server.get("ip");
            this.port = (String) server.get("port");

            this.actions = (JSONArray) jsonObject.get("actions");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startClient() {
//        s = new Socket("localhost", 4999);
//        pr = new PrintWriter(s.getOutputStream());
//
//
//        // Constantly checking for new messages
//        while (true) {
//
//            // Send get mesages request to server
//            pr.println(2);
//            pr.println("abc");
//
//            // Send message
//            pr.flush();
//
//            try {
//                // Read message from Server
//                InputStreamReader in = new InputStreamReader(s.getInputStream());
//                BufferedReader bf = new BufferedReader(in);
//
//                int messagesNumber = Integer.parseInt(bf.readLine());
//
//                if (messagesNumber != 500) {
//                    for (int i = 0; i < messagesNumber; i++) {
//                        String message = bf.readLine();
//                        System.out.println("Message " + i + " : " + message);
//                    }
//                }
//            } catch (IOException e) {
//                System.out.println(e);
//            }
//        }

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


}

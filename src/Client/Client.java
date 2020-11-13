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

    private InputStreamReader in;
    private BufferedReader bf;

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
            this.id = (String) person.get("id");
            ;
            this.name = (String) person.get("name");

            JSONObject keys = (JSONObject) person.get("keys");
            this.privateKey = (String) keys.get("private");
            this.publicKey = (String) keys.get("public");

            JSONObject server = (JSONObject) jsonObject.get("server");
            this.ip = (String) server.get("ip");
            this.port = (String) server.get("port");

            this.actions = (JSONArray) jsonObject.get("actions");

            // REGISTRATION
            s = new Socket("localhost", 4999);
            pr = new PrintWriter(s.getOutputStream());

            pr.println(1);
            pr.println(id);
            pr.println(name);
            pr.println(name);
            pr.println(publicKey);
            pr.flush();

            // Read message from Server
            in = new InputStreamReader(s.getInputStream());
            bf = new BufferedReader(in);

            System.out.println(bf.readLine());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void startClient() throws IOException {
        long startingTime = System.currentTimeMillis();


        // EXECUTE ACTIONS
        for (int i = 0; i < this.actions.size(); i++) {
            s = new Socket("localhost", 4999);
            pr = new PrintWriter(s.getOutputStream());

            // Read message from Server
            in = new InputStreamReader(s.getInputStream());
            bf = new BufferedReader(in);

            String a = this.actions.get(i).toString();
            String[] parts = a.split("\\[", 3);

            String actionType = parts[0];
            actionType = actionType.replace(" ", "");

            String toId = parts[1];
            toId = toId.replace("] ", "");

            String m = parts[2];
            m = m.replace("]", "");


            if (actionType.equals("SEND")) {
                if (!toId.contains(",")) {
                    pr.println(3);
                    pr.println(this.id);
                    pr.println(toId);
                    pr.println(m);
                    pr.flush();


                    System.out.println(bf.readLine());
                } else {


                    String[] name = toId.split(",", 2);
                    String firstName = name[0].replace(" ", "");
                    String lastName = name[1].replace(" ", "");

                    pr.println(4);
                    pr.println(this.id);
                    pr.println(firstName);
                    pr.println(lastName);
                    pr.println(m);
                    pr.flush();

                    System.out.println(bf.readLine());
                }
            }

        }

        // CHECK FOR MESSAGES
        double milliSeconds = Double.parseDouble(this.duration) * 1000;
        System.out.println("checking for messages..");
        // Constantly checking for new messages
        while (System.currentTimeMillis() < startingTime + milliSeconds) {


            s = new Socket("localhost", 4999);
            pr = new PrintWriter(s.getOutputStream());

            // Read message from Server
            in = new InputStreamReader(s.getInputStream());
            bf = new BufferedReader(in);

            // Send get mesages request to server
            pr.println(2);
            pr.println(this.id);

            // Send message
            pr.flush();

            try {

                int messagesNumber = Integer.parseInt(bf.readLine());

                if (messagesNumber != 500) {
                    for (int j = 0; j < messagesNumber; j++) {
                        String message = bf.readLine();
                        System.out.println("Message " + j + " : " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        System.out.println("duration over");
    }
    

}

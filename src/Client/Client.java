package Client;

import Shared.Packet;
import Shared.PacketLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class Client {

    private final PacketLogger pktLog;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

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
        pktLog = new PacketLogger();

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
            this.name = (String) person.get("name");

            JSONObject keys = (JSONObject) person.get("keys");
            this.privateKey = (String) keys.get("private");
            this.publicKey = (String) keys.get("public");

            JSONObject server = (JSONObject) jsonObject.get("server");
            this.ip = (String) server.get("ip");
            this.port = (String) server.get("port");

            this.actions = (JSONArray) jsonObject.get("actions");

            Socket s = null;
            while(s ==null) {
                // REGISTRATION
                try {
                    s = new Socket("localhost", 4999);
                    System.out.println("Timeout on connection request"); //Server was not open yet probably
                } catch (ConnectException e) {
                    Thread.sleep(100);
                }
            }
            objectInputStream = new ObjectInputStream(s.getInputStream());
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());

            objectOutputStream.writeObject(pktLog.newOut(
                    new Packet(1,
                            id,
                            null,
                            null,
                            name,
                            name,
                            publicKey)));

            // Read message from Server
            pktLog.newIn(objectInputStream.readObject());
            System.out.println(pktLog.getLastIn().getData());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void startClient() throws IOException, ClassNotFoundException {
        long startingTime = System.currentTimeMillis();


        // EXECUTE ACTIONS
        for (Object action : this.actions) {
//            s = new Socket("localhost", 4999); This is bad since there is already an open socket which has not yet been
//            closed which will cause memory leaks!

            // Read message from Server
            pktLog.newIn(objectInputStream.readObject());

            String a = action.toString();
            String[] parts = a.split("\\[", 3);

            String actionType = parts[0];
            actionType = actionType.replace(" ", "");

            String toId = parts[1];
            toId = toId.replace("] ", "");

            String m = parts[2];
            m = m.replace("]", "");


            if (actionType.equals("SEND")) {
                if (!toId.contains(",")) {
                    objectOutputStream.writeObject(pktLog.newOut(
                            new Packet(3,
                                    id,
                                    toId,
                                    m,
                                    name,
                                    name,
                                    null)));
                } else {

                    String[] name = toId.split(",", 2);
                    String firstName = name[0].replace(" ", "");
                    String lastName = name[1].replace(" ", "");

                    objectOutputStream.writeObject(pktLog.newOut(
                            new Packet(4,
                                    id,
                                    ip,
                                    m,
                                    firstName,
                                    lastName,
                                    null)));
                    System.out.println(pktLog.newIn(objectInputStream.readObject()));
                }
            }

        }

        // CHECK FOR MESSAGES
        double milliSeconds = Double.parseDouble(this.duration) * 1000;
        System.out.println("checking for messages..");
        // Constantly checking for new messages
        while (System.currentTimeMillis() < startingTime + milliSeconds) {


//            s = new Socket("localhost", 4999); Same story here, risk of memory leaks!

            // Read message from Server
            pktLog.newIn(objectInputStream.readObject());

            // Send get messages request to server
            objectOutputStream.writeObject(pktLog.newOut(
                    new Packet(2,
                            id,
                            null,
                            null,
                            null,
                            null,
                            null)));
            try {
                pktLog.newIn(objectInputStream.readObject());
                int messagesNumber = pktLog.getLastIn().getType();

                if (messagesNumber != 500) {
                    for (int j = 0; j < messagesNumber; j++) {
                        String message = pktLog.getLastIn().getData();
                        System.out.println("Message " + j + " : " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("duration over");
    }
    

}

package Server;

import Shared.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestClient {
    private static boolean running;
    public static void main(String[] args) {
        try {
            System.out.println("Launching client");
            Socket sock = new Socket("localhost",4999);
            running = true;
            ObjectOutputStream OOS = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream OIS = new ObjectInputStream(sock.getInputStream());

            OOS.writeObject(new Packet(0,
                    "localhost",
                    "localhost",
                    "Hello from client",
                    "Luc",
                    "Sparidans",
                    "Key"));
            while(running){
                Packet p = (Packet)OIS.readObject();
                if(p!=null){
                    System.out.println(p.getData());

                    running = false;
                    sock.close();
                    OOS.close();
                    OIS.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}

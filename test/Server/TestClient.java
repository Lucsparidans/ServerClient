package Server;

import Shared.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try {
            System.out.println("Launching client");
            Socket sock = new Socket("localhost", 4999);
            boolean running = true;
            ObjectOutputStream OOS = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream OIS = new ObjectInputStream(sock.getInputStream());

            OOS.writeObject(new Packet(Packet.PacketType.SYN,
                    "localhost",
                    "localhost",
                    "Hello from client",
                    Packet.DataFormat.STRING,
                    "Luc",
                    "Sparidans",
                    "Key"));
            while(running){
                Packet p = (Packet)OIS.readObject();
                if (p != null) {
                    System.out.println(p.getData());

                    running = false;
                    sock.close();
                    OOS.close();
                    OIS.close();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}

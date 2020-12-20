package Client;

import Shared.Packet;
import Shared.Packet.PacketType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    private static boolean running;
    public static void main(String[] args) {
        // First setup a open port in order to accept connections
        try {
            System.out.println("Launching server");
            ServerSocket ss = new ServerSocket(4999);
            Socket sock = ss.accept();
            running = true;
            ObjectOutputStream OOS = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream OIS = new ObjectInputStream(sock.getInputStream());

            while(running){
                Packet p = (Packet)OIS.readObject();
                if(p!=null) {
                    System.out.println(p.getData());
                    OOS.writeObject(new Packet(PacketType.ACK,
                            "localhost",
                            "localhost",
                            "Package successfully received!",
                            Packet.DataFormat.STRING,
                            "Luc",
                            "Sparidans",
                            "Key"
                    ));

                    running = false;
                    sock.close();
                    ss.close();
                    OOS.close();
                    OIS.close();
                }
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

package Client;

import Server.ClientHandler;
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
            ClientHandler ch = new ClientHandler(sock);
            new Thread(ch).start();

            running = true;
            ObjectInputStream OIS = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream OOS = new ObjectOutputStream(sock.getOutputStream());

            while(running){
                Packet p = (Packet)OIS.readObject();
                if(p!=null) {
                    System.out.println(p.getData());
                    OOS.writeObject(new Packet(PacketType.ACK,
                            "localhost",
                            "localhost",
                            "Package successfully received!",
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

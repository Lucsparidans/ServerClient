package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(4999);
        Socket s = ss.accept();

        System.out.println("Client Connected");

        // Read message from client
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        String str = bf.readLine();

        System.out.println("client: " + str);

        // Send message to Client
        PrintWriter pr = new PrintWriter(s.getOutputStream());
        pr.println("Yes It Is!");
        pr.flush();
    }
}

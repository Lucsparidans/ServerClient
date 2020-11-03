package Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost",4999);

        // Send message to Server
        PrintWriter pr = new PrintWriter(s.getOutputStream());
        pr.println("Is it Working?");
        pr.flush();

        // Read message from Server
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        String str = bf.readLine();

        System.out.println("server: " + str);
    }
}

import Client.App;
import Server.Server;

import java.io.IOException;

public class LauncherTest {
    public static void main(String[] args) {

            // Must be multi threaded
            Thread t1 = new Thread(() -> {
                try {
                    Server.main(args);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            Thread t2 = new Thread(() -> App.main(args));
            t1.start();
            t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

import Client.App;
import Server.Server;

public class LauncherTest {
    public static void main(String[] args) {

        // Must be multi threaded
        Thread t1 = new Thread(() -> Server.main(args));
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

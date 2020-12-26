package Client;


import java.io.IOException;

public class App {
    public static void main(String[] args) {
        Client c = new Client("src/JSON_files/config_1.json");
        Client c2 = new Client("src/JSON_files/config_2.json");

        try {
            c.startClient();
            c2.startClient();
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}

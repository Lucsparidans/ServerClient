package Client;


public class App {
    public static void main(String[] args) {
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        Client c = new Client("src/JSON_files/config_1.json");
        Client c2 = new Client("src/JSON_files/config_2.json");
        Client c3 = new Client("src/JSON_files/config_3.json");
        Client c4 = new Client("src/JSON_files/config_4.json");
        Client c5 = new Client("src/JSON_files/config_5.json");
        new Thread(c).start();
        new Thread(c2).start();
        new Thread(c3).start();
        new Thread(c4).start();
        new Thread(c5).start();
    }
}

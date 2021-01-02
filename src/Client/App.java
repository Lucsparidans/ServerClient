package Client;


public class App {
    public static void main(String[] args) {
        try{
            Thread.sleep(100);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        Client c = new Client("src/JSON_files/config_1.json");
        Client c2 = new Client("src/JSON_files/config_2.json");
        new Thread(c).start();
        new Thread(c2).start();
    }
}

package Client;


public class App {
    public static void main(String[] args) {
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        for (int i = 1; i < 6; i++) {
            new Thread(new Client(String.format("src/JSON_files/config_%d.json",i))).start();
        }
    }
}

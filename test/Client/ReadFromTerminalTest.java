package Client;

import java.util.Scanner;

public class ReadFromTerminalTest {
    public static void main(String[] args) {
        new Thread(new InputHandler()).start();
        while (true) {
            try {
                System.out.println("Iteration");
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    private static class InputHandler implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            System.out.println(scanner.next());
        }
    }
}

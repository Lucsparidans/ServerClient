import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

public class SynchronisationTest {

    private static final ArrayList<Boolean> BOOLS = new ArrayList<>();
    public static final Random RNG = new Random();
    private static final int LIST_LENGTH = 10;
    public static final int LOOPS = 10;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        fillDataStructure();
        printDataStructure();
        // TODO: Create two threads that try to access the same data-structure at the same time.
        Thread t1 = new Thread(new Accessor(),"Thread-1");
        Thread t2 = new Thread(new Accessor(), "Thread-2");
        t1.start();
        t2.start();
    }

    private static class Accessor implements Runnable{
        @Override
        public void run() {
            for (int i = 0; i < LOOPS; i++) {
                BOOLS.set(RNG.nextInt(LIST_LENGTH), Boolean.TRUE);
                synchronized (lock) {
                    System.out.printf("Thread: %s \n", Thread.currentThread().getName());
                    printDataStructure();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void fillDataStructure(){
        IntStream.range(0,LIST_LENGTH).parallel().forEach(i -> BOOLS.add(Boolean.FALSE));
    }

    private static void printDataStructure(){
        System.out.println("\n+++++++++++ ARRAYLIST +++++++++++");
        IntStream.range(0,LIST_LENGTH).parallel().forEachOrdered(i-> System.out.printf("Index %d has value: %b\n",i,BOOLS.get(i)));
        System.out.println("+++++++++++++++++++++++++++++++++\n");
    }


}

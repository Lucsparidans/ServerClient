package Shared;

public abstract class ConsoleLogger {
    private static final boolean DEBUG = true;
    public static void LogMessage(String message, Object... args){
        if(DEBUG)
            System.out.printf(Thread.currentThread().getName() + ": "+ message + "\n",args);
    }
    public static void LogMessage(String message){
        if(DEBUG)
            System.out.println(Thread.currentThread().getName() + ": " + message);
    }
    public static void LogMessage(Object o){
        if(DEBUG){
            System.out.printf("%s: %s\n",Thread.currentThread().getName(),o.toString());
        }
    }
}

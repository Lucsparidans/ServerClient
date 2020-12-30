package Shared;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class FileLogger {
    private static final String baseDirTraffic = "logs/traffic/";
    private static final String baseDirMessages = "logs/messages/";

    public synchronized static void writeLogToFile(String log){
        try {
            String newFileName = baseDirTraffic + getTimeStamp();
            makeFile(log, newFileName);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public synchronized static void writeMessagesToFile(String message, String clientName){
        try{
            String newFileName = baseDirMessages + clientName + getTimeStamp();
            makeFile(message, newFileName);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public synchronized static void writeMessagesToFile(ArrayList<String> messages, String clientName){
        try{
            String newFileName = baseDirMessages + clientName + getTimeStamp();
            makeFile(formatAsString(messages), newFileName);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private static void makeFile(String data, String newFileName) throws IOException {
        File f = new File(newFileName);
        int cnt = 1;
        while(f.exists()){
            f = new File(String.format("%s(%d).txt",newFileName,cnt++));
        }
        FileWriter fileWriter = new FileWriter(f);
        fileWriter.write(data);
        fileWriter.close();
    }

    private static String getTimeStamp(){
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }

    private static String formatAsString(ArrayList<String> messages){
        StringBuilder sb = new StringBuilder();
        messages.forEach(m -> sb.append(m).append("\n"));
        return sb.toString();
    }
}

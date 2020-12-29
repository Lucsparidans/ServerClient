package Shared;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class FileLogger {
    private static final String baseDir = "logs/";
    public synchronized static void writeLogToFile(String log){
        try {
            String newFileName = baseDir + getName();
            File f = new File(newFileName);
            int cnt = 1;
            while(f.exists()){
                f = new File(String.format("%s(%d).txt",newFileName,cnt++));
            }
            FileWriter fileWriter = new FileWriter(f);
            fileWriter.write(log);
            fileWriter.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private static String getName(){
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }
}

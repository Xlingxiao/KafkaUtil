package producer.FTP.FTPThread;

import java.io.BufferedReader;
import java.io.IOException;

public class ProcessContent {
    public static StringBuilder startProcess(BufferedReader br){
        String msg ;
        StringBuilder sb = new StringBuilder();
//      逐行读取到StringBulider中
        try{
            while ((msg=br.readLine())!= null) {
                sb.append(msg);
                sb.append("\r\n");
            }
            System.out.println(sb);
        }catch (IOException e){
            sb = null;
            e.printStackTrace();
        }
        return sb;
    }
}

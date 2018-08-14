package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DownloadFile {
    public BufferedReader download(FTPClient ftpClient,String url){
        FTPFile file ;
        InputStream is ;
        String name ;
        BufferedReader br =null;
        try {
            file = ftpClient.mlistFile(url);
//            将文件名转为iso-8859-1
            name = new String(file.getName().getBytes("UTF-8"),"iso-8859-1");
            is = ftpClient.retrieveFileStream(name);
            br = new BufferedReader(new InputStreamReader(is,"utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return br;
    }
}

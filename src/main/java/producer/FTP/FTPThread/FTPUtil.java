package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.kafka.clients.producer.Producer;


import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Properties;


public class FTPUtil {
    public static FTPClient getFtpClient() {
        //获取FTP对象
        Properties props = new Properties();
        InputStream is = FTPUtil.class.getClassLoader().getResourceAsStream("FTP.properties");
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = props.getProperty("url");
        Integer port = Integer.valueOf(props.getProperty("port"));
        String username = props.getProperty( "username");
        String password =  props.getProperty("password");
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.connect(url, port);
            ftp.login(username, password);//登录
//            获取链接状态码
            reply = ftp.getReplyCode();
//            验证链接状态码
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return null;
            }
            ftp.setControlEncoding("UTF-8");
//            nat网络环境下需要设置为被动模式
            ftp.enterLocalPassiveMode();
        } catch (SocketException e){
            System.out.println("连接ftp服务器出错");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftp;
    }

    private static String AllFilePath(FTPClient ftpClient, String path, Producer<String,String> producer, String topic){
        try {
//            FTP协议默认只支持iso-8859-1的编码格式，
//            这里我们转换中文文件名为字节形式
//            将字节形式转为iso-8859-1的编码
            path = new String(path.getBytes("UTF-8"),"iso-8859-1");
//            判断改变工作路径是否成功
            boolean ff = ftpClient.changeWorkingDirectory(path);
            if(ff){
                FTPFile[] fs = ftpClient.listFiles();
//                    只获取服务器上的前十个文件
                for(int i =0;i<7;i++){
//                    判断文件路径下没有那么多文件的话就退出
                    if (fs.length<=i)
                        break;
                    FTPFile file = fs[i];
                    System.out.println(file.getName());
                    if (file.isDirectory()){
                        AllFileText(ftpClient,file.getName(),producer, topic);
                    }
                    else if(file.isFile()){
//                        将文件名转为iso-8859-1
                        String name = new String(file.getName().getBytes("UTF-8"),"iso-8859-1");
                        InputStream is = ftpClient.retrieveFileStream(name);
                        BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
//                    解析ftp文件
                        StringBuilder sb = ProcessContent(br);
                        br.close();
                        is.close();
                        ftpClient.completePendingCommand();
//                      这里调用了上面的SendOneFile里面的Producer方法
                        StartProducer(producer,topic,sb);
                    }
                    else
                        break;
                }
//                遍历一个目录之后退出
                ftpClient.changeToParentDirectory();
            }
            else{
                System.out.println("更改FTP工作路径失败！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void endFtp(FTPClient ftpClient){
        try {
            ftpClient.disconnect();
            System.out.println("成功关闭与FTP服务器的连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

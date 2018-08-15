package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.kafka.clients.producer.Producer;


import java.io.*;

import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class FTPUtil {

    //获取FTP对象
    public FTPClient getFtpClient() {
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
            return ftp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftp;
    }

//    遍历目录添加到队列之中
    public void AllFilePath(FTPClient ftpClient,BlockingQueue queue, String path){
        Lock lock = new ReentrantLock();
        try {
//            FTP协议默认只支持iso-8859-1的编码格式，
//            这里我们转换中文文件名为字节形式
//            将字节形式转为iso-8859-1的编码
            path = new String(path.getBytes("UTF-8"),"iso-8859-1");
//            判断改变工作路径是否成功
            boolean ff = ftpClient.changeWorkingDirectory(path);
            if(ff){
                FTPFile[] fs = ftpClient.listFiles();
//                遍历文件
//                for(FTPFile file:fs){
                for(int i = 0; i<20;i++){
                    if (fs.length<=i)
                        break;
                    FTPFile file = fs[i];
                    if (file.isDirectory()){
                        this.AllFilePath(ftpClient,queue,ftpClient.printWorkingDirectory()+"/"+file.getName());
                    }
                    else if(file.isFile()){
//                        将文件名转为iso-8859-1
                        path = new String(ftpClient.printWorkingDirectory()
                                .getBytes("iso-8859-1"),"UTF-8")
                                +"/"+file.getName();
                        if (queue.contains(path))
                            continue;
                        lock.lock();
                        if (queue.contains(path))
                            continue;
                        queue.put(path);
                        lock.unlock();
                        if (queue.size()%20==0)
                            System.out.println("目前的队列长度："+queue.size());
                    }
                }
//                遍历一个目录之后退出
                ftpClient.changeToParentDirectory();
            }
            else{
                System.out.println("更改FTP工作路径失败！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println(path+"添加队列失败！");
            e.printStackTrace();
        }
    }

//    得到队列中一个文件的内容
    public StringBuilder getDownlod(FTPClient ftpClient,String path){
        BufferedReader br = null;
        StringBuilder sb =null;
        try {
            System.out.println(path);
            path = new String(path.getBytes("UTF-8"),"iso-8859-1");
            InputStream is = ftpClient.retrieveFileStream(path);
            if(null==is){
                System.out.println("输入资源地址出错！");
                return sb;
            }
            br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String msg ;
            sb =new StringBuilder();
            while (null!=(msg=br.readLine())){
                sb.append(msg);
            }
            br.close();
            is.close();
            if (!ftpClient.completePendingCommand())
                ;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

//    关闭FTP对象
    public void endFtp(FTPClient ftpClient){
        try {
            ftpClient.disconnect();
            System.out.println("成功关闭与FTP服务器的连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

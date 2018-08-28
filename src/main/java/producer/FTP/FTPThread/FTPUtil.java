package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


import java.io.*;

import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class FTPUtil {

//    获取FTP对象
    public FTPClient getFtpClient() {
//        读取配置文件
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
//        根据配置文件创建FTP对象
        FTPClient ftp = new FTPClient();
        try {
            int reply;
//            如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.connect(url, port);
            ftp.login(username, password);//登录
//            获取链接状态码
            reply = ftp.getReplyCode();
//            验证链接状态码
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return null;
            }
            ftp.setControlKeepAliveReplyTimeout(10*1000);
            ftp.setDataTimeout(10*1000);
//            ftp.setControlEncoding("UTF-8");
//            nat网络环境下需要设置为被动模式
            ftp.enterLocalPassiveMode();
        } catch (SocketException e){
            System.out.println("连接ftp服务器出错");
            return null;
        } catch (IOException e) {
            System.out.println("获取配置文件出错");
            return null;
        }
        return ftp;
    }

    /**
     *@描述 遍历目录添加到队列之中
     *@参数  [FTPClient ftpClient,BlockingQueue queue, String path]
     *@返回值  void
     *@创建人  Lingxiao
     *@创建时间  2018/8/28
     */
    public void AllFilePath(FTPClient ftpClient,BlockingQueue queue, String path){
        Lock lock = new ReentrantLock();
        try {
//            FTP协议默认只支持iso-8859-1的编码格式，
//            这里我们转换中文文件名为字节形式
//            将字节形式转为iso-8859-1的编码
//            path = new String(path.getBytes("UTF-8"),"iso-8859-1");
//            判断改变工作路径是否成功
            boolean ff = ftpClient.changeWorkingDirectory(path);
            if(ff){
                FTPFile[] fs = ftpClient.listFiles();
//                遍历文件,下面两行可以选择遍历的深度和次数
                for(FTPFile file:fs){
//                for(int i = 0; i<20;i++){
//                    如果文件夹中没有指定的文件数量就不再遍历下去
//                    if (fs.length<=i)
//                        break;
//                    FTPFile file = fs[i];
//                    如果判断文件为文件夹递归调用这个函数
                    if (file.isDirectory()){
                        this.AllFilePath(ftpClient,queue,ftpClient.printWorkingDirectory()+"/"+file.getName());
                    }
                    else if(file.isFile()){
//                        将文件名由iso-8859-1转为UTF-8
//                        path = new String(ftpClient.printWorkingDirectory()
//                                .getBytes("iso-8859-1"),"UTF-8")
//                                +"/"+file.getName();
                        path = ftpClient.printWorkingDirectory()+"/"+file.getName();
//                        如果队列中已经包含了这个路径就不再将path加入
                        if (queue.contains(path))
                            continue;
                        lock.lock();
                        if (queue.contains(path))
                            continue;
                        queue.put(path);
//                        System.out.println(new String(path.getBytes("iso-8859-1"),"utf-8"));
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


    /**
     * 根据path获取FTP服务器上的文件内容
     * @param ftpClient FTP对象
     * @param path 目标文件全路径
     * @param retries 传输文件失败后重试次数
     * @return
     */
    public StringBuilder getDownload(FTPClient ftpClient, String path, int retries){
        BufferedReader br = null;
        StringBuilder sb =null;
        try {
//            打印文件绝对路径
//            System.out.println(path);
//            将文件路径转码
//            path = new String(path.getBytes("UTF-8"),"iso-8859-1");
//            获取文件输入流
            InputStream is = ftpClient.retrieveFileStream(path);
//            通过文件的绝对路径没有获取到输入流
//            （文件之前存在之后被删除了）
//            获取到的StringBuilder就为空
            if(null==is){
                System.out.printf("获取资源输入流出错！%s\n",path);
                return sb;
            }
//            使用utf-8的编码方式解码文件内容
            br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String msg ;
            sb =new StringBuilder();
            while (null!=(msg=br.readLine())){
                sb.append(msg);
            }
            br.close();
            is.close();
//            使用ftpClient.retrieveFileStream(path);
//            获取数据之后 必须 调用ftpClient.completePendingCommand()
//            验证本次数据是否完成传输
            if (!ftpClient.completePendingCommand()){
//            如果传输失败重试retries次
                if (retries>0){
//                    path = new String(path.getBytes("iso-8859-1"),"UTF-8");
                    sb = getDownload(ftpClient,path,retries-1);
                }
                else{
                    System.out.printf("文件%s再下载重试次数范围内均下载失败\n",path);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

//    关闭FTP对象
    public void endFtp(FTPClient ftpClient,String name){
        try {
            ftpClient.disconnect();
            System.out.println(name+":关闭与FTP服务器的连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

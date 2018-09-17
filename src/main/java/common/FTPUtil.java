package common;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class FTPUtil {

    //    读取FTP配置问价的对象
    private Properties props = new Properties();

    //    获取FTP对象
    public FTPClient getFtpClient() {
//        读取配置文件
        props = new Properties();
        InputStream is = FTPUtil.class.getClassLoader().getResourceAsStream("FTP/FTP.properties");
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = props.getProperty("url");
        Integer port = Integer.valueOf(props.getProperty("port"));
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        Integer dataTimeout = Integer.valueOf(props.getProperty("DataTimeout"));
        Integer controlKeepAliveReplyTimeout = Integer.valueOf(props.getProperty("ControlKeepAliveReplyTimeout"));
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
//            设置客户端在10秒内没有操作就断开服务
            ftp.setControlKeepAliveReplyTimeout(dataTimeout * 1000);
            ftp.setDataTimeout(controlKeepAliveReplyTimeout * 1000);
//            ftp.setControlEncoding("UTF-8");
//            nat网络环境下需要设置为被动模式
            ftp.enterLocalPassiveMode();
        } catch (SocketException e) {
            System.out.println("连接ftp服务器出错");
            return null;
        } catch (IOException e) {
            System.out.println("获取配置文件出错");
            return null;
        }
        return ftp;
    }

    /**
     * @描述 遍历目录添加到队列之中
     * @参数 [FTPClient ftpClient,BlockingQueue queue, String path]
     * @返回值 void
     * @创建人 Ling
     * @创建时间 2018/8/28
     */
    public void AllFilePath(FTPClient ftpClient, BlockingQueue queue, String path, long lastTime, long thisTime) {
//        用来锁队列,防止出现多个线程同时传输相同对象进入队列
        Lock lock = new ReentrantLock();
        try {
//            判断是否切换工作空间成功,切换成功后再进行后续处理
            boolean ff = ftpClient.changeWorkingDirectory(path);
            if (ff) {
                FTPFile[] fs = ftpClient.listFiles();
//                遍历文件
                for (FTPFile file : fs) {
//                    文件是文件夹
                    if (file.isDirectory()) {
//                    递归调用,将工作空间设置为当前工作空间+当前文件名
                        this.AllFilePath(ftpClient, queue, ftpClient.printWorkingDirectory() + "/" + file.getName(),
                                lastTime, thisTime);
                    }
//                    文件是文件
                    else if (file.isFile()) {
//                        查看文件最后修改时间,因为我的服务器上通过ftp下载后的文件时间会减少8个小时所以这里给他加回去
                        long fileTime = file.getTimestamp().getTimeInMillis() + 28800000;
//                        文件最后修改时间在上次程序运行时间和本次程序开始时间之间的文件添加到队列中
                        if (fileTime >= lastTime && fileTime < thisTime) {
//                            真正的path是文件路径路径加上文件名
                            path = ftpClient.printWorkingDirectory() + "/" + file.getName();
//                            如果队列中已经包含了这个路径就不再将path加入
                            if (queue.contains(path))
                                continue;
                            lock.lock();
                            if (queue.contains(path)) {
                                lock.unlock();
                                continue;
                            }
                            queue.put(path);
//                            打印一下程序开始时间 文件最后修改时间 程序上次开始时间
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            System.out.println(new String(file.getName().getBytes("iso-8859-1"), "utf-8")
                                    + "\tFileTime: " + format.format(file.getTimestamp().getTime())
                                    + "\t程序上次完成时间\t" + format.format(new Date(lastTime))
                                    + "\t程序本次开始时间\t" + format.format(new Date(thisTime))
                            );
                            lock.unlock();
//                            打印队列长度
                            if (queue.size() % 20 == 0)
                                System.out.println("目前的队列长度：" + queue.size());
                        }
                    }
                }
//                遍历一个目录之后退出
                ftpClient.changeToParentDirectory();
            } else {
                System.out.println("更改FTP工作路径失败！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            try {
                System.out.println(
                        new String(path.getBytes("iso-8859-1"), "utf-8") + "添加队列失败！");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    /**
     * 根据path获取FTP服务器上的文件内容
     *
     * @param ftpClient FTP对象
     * @param path      目标文件全路径
     * @param retries   传输文件失败后重试次数
     * @return StringBuilder 服务器上下载的内容
     */
    public StringBuilder getDownload(FTPClient ftpClient, String path, int retries) {
        BufferedReader br;
        StringBuilder stringBuilder = null;
        try {
//            获取文件输入流
            InputStream is = ftpClient.retrieveFileStream(path);
//            使用utf-8的编码方式解码文件内容
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            stringBuilder = new StringBuilder();
            String msg;
            while (null != (msg = br.readLine())) {
//                msg = msg.replaceAll("\\s*","");
                stringBuilder.append(msg).append("\n");
            }
            br.close();
            is.close();
//            使用ftpClient.retrieveFileStream(path);
//            获取数据之后 必须 调用ftpClient.completePendingCommand()
//            验证本次数据是否完成传输
            if (!ftpClient.completePendingCommand()) {
//            如果传输失败重试retries次
                if (retries > 0) {
                    System.out.printf("重试第%d次\n", Integer.valueOf(props.getProperty("ftpFileRetries")) - retries + 1);
                    stringBuilder = getDownload(ftpClient, path, retries - 1);
                } else {
                    System.out.printf("文件%s再下载重试次数范围内均下载失败\n",
                            new String(path.getBytes("iso-8859-1"), "utf-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println("转码异常");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("文件获取异常");
            if (retries > 0) {
                System.out.printf("重试第%d次\n", Integer.valueOf(props.getProperty("ftpFileRetries")) - retries + 1);
                stringBuilder = getDownload(ftpClient, path, retries - 1);
            } else {
                try {
                    System.out.printf("文件%s再下载重试次数范围内均下载失败\n",
                            new String(path.getBytes("iso-8859-1"), "utf-8"));
                } catch (UnsupportedEncodingException e1) {
                    System.out.println("转码失败");
//                    e1.printStackTrace();
                }
            }
        }
        return stringBuilder;
    }

    //    关闭FTP对象
    public void endFtp(FTPClient ftpClient, String name) {
        try {
            if (ftpClient != null && ftpClient.isConnected())
                ftpClient.disconnect();
            System.out.println(name + ":关闭与FTP服务器的连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

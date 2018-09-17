package producer.FTP;

import common.FTPUtil;
import org.apache.commons.net.ftp.FTPClient;

import java.util.concurrent.BlockingQueue;

/**
 * @program: KafkaUtil
 * @description: 得到FTP服务器上指定的文件路径
 * @author: Ling
 * @create: 2018/08/28 09:43
 **/
class filePathProducer implements Runnable {
//    存储文件路径的队列长度
    private BlockingQueue queue;
//    初始文件夹路径
    private String initPath;
//    程序上次执行完成时间
    private long lastTime;
//    程序本次执行开始时间
    private long thisTime;
    filePathProducer(BlockingQueue queue, String initPath, long lastTime, long thisTime) {
        this.queue = queue;
        this.initPath = initPath;
        this.lastTime = lastTime;
        this.thisTime = thisTime;
    }
    /**
     * 创建FTP对象
     * 调用AllFilePath(ftpClient,queue,initPath);获取指定路径下所有文件路径
     */
    public void run() {
//        System.out.printf("生产文件路径者 %s启动\n",Thread.currentThread().getName());
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null)
//            开始获取FTP服务器上的文件路径
            util.AllFilePath(ftpClient,queue,initPath,lastTime,thisTime);
        else System.out.println("创建Ftp连接失败，退出");
//        关闭FTP连接
        util.endFtp(ftpClient, Thread.currentThread().getName());
        System.out.println(Thread.currentThread().getName()+"处理结束---------");
    }
}
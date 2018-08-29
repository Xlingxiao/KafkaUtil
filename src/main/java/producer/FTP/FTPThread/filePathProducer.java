package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;

import java.util.concurrent.BlockingQueue;

/**
 * @program: KafkaUtil
 * @description: 得到FTP服务器上指定的文件路径
 * @author: Ling
 * @create: 2018/08/28 09:43
 **/
class filePathProducer implements Runnable {
    private BlockingQueue queue;
    private String initPath ;
    filePathProducer(BlockingQueue queue, String initPath) {
        this.queue = queue;
        this.initPath = initPath;
    }
    /**
     * 创建FTP对象
     * 调用AllFilePath(ftpClient,queue,initPath);获取指定路径下所有文件路径
     */
    public void run() {
        System.out.printf("生产文件路径者 %s启动\n",Thread.currentThread().getName());
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null)
            util.AllFilePath(ftpClient,queue,initPath);
        else System.out.println("创建Ftp连接失败，退出");
//        关闭FTP连接
        util.endFtp(ftpClient,Thread.currentThread().getName());
        System.out.println(Thread.currentThread().getName()+"处理结束---------");
    }
}
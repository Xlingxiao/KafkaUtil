package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;
import producer.demo.myProducer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: KafkaUtil
 * @description: 根据queue中的地址下载FTP服务器上的内容
 * @author: Ling
 * @create: 2018/08/28 09:44
 **/
class filePathConsumer implements Runnable {
    /**
     * @param retries 控制Ftp工具从FTP服务器下载文件失败后的重试次数
     * @param queue 主类中传过来的队列，里面包含FTP服务器上的文件路径
     * @param topic 需要发送到kafka 的topic
     * @param lock 线程锁
     */
    int retries = 3;
    BlockingQueue<String> queue;
    String topic;
    Lock lock = new ReentrantLock();
    public filePathConsumer(BlockingQueue queue, String topic) {
        this.queue = queue;
        this.topic = topic;
    }

    public void run() {
        System.out.printf("消费文件路径者 %s 启动\n",Thread.currentThread().getName());
//        刚开始时队列为空所有消费者都开始等待
        while (queue.size()<50){}
//        获取FTPClien对象
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null){
//            处理文件内容对象
            ProcessContent process = new ProcessContent();
//            发送处理过的文件内容的对象
            myProducer myproducer = new myProducer();
            StringBuilder sb = null;
            while (!queue.isEmpty()){
//            如果队列是空的不进行下面的操作
                lock.lock();
                if (queue.isEmpty()){
                    lock.unlock();
                    continue;
                }
                String path = this.queue.poll();
                lock.unlock();
//                下载文件内容,指定文件下载失败后的重试次数
                sb = util.getDownlod(ftpClient,path,retries);
                if (sb==null) continue;
//                对文件内容进行处理
                sb = process.startProcess(sb);
//                查看处理后的文件第一个“，”之前的内容
                System.out.println(sb.toString().split(",",2)[0]);
//                发送处理过的内容
                myproducer.sendMsg(this.topic,sb);
//            如果队列为空等待2秒钟
                if (queue.isEmpty()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
//            关闭FTP连接
            util.endFtp(ftpClient);
        }
        else System.out.println("退出");
    }
}

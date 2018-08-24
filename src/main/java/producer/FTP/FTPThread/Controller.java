package producer.FTP.FTPThread;

import org.apache.commons.net.ftp.FTPClient;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class Controller {
    public static void main(String[] args) {
        String topic = "test";
        String initPath = "./code/jupyter";
        BlockingQueue queue = new ArrayBlockingQueue(1000);
        getFilePath getPath = new getFilePath(queue,initPath);
        consumerFilePath consumerPath = new consumerFilePath(queue,topic);
        for (int i =0;i<1;i++){
            new Thread(getPath,"生产者-"+i).start();
        }
        for (int i =0;i<15;i++){
            new Thread(consumerPath,"消费者-"+i).start();
        }
    }
}
class getFilePath implements Runnable {
    BlockingQueue queue;
    String initPath ;
    public getFilePath(BlockingQueue queue,String initPath) {
        this.queue = queue;
        this.initPath = initPath;
    }

    public void run() {
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null)
        util.AllFilePath(ftpClient,queue,initPath);
        else System.out.println("退出");
    }
}

class consumerFilePath implements Runnable {
    BlockingQueue<String> queue;
    String topic;
    Lock lock = new ReentrantLock();

    public consumerFilePath(BlockingQueue queue, String topic) {
        this.queue = queue;
        this.topic = topic;
    }

    public void run() {
        while (queue.isEmpty()){}
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null){
            ProcessContent process = new ProcessContent();
            myProducer myproducer = new myProducer();
            StringBuilder sb = null;
            while (!queue.isEmpty()){
                lock.lock();
                if (queue.isEmpty()){
                    lock.unlock();
                    continue;
                }
                String path = this.queue.poll();
                lock.unlock();
                sb = util.getDownlod(ftpClient,path);
                sb = process.startProcess(sb);
                System.out.println(sb.toString().split(",",2)[0]);
                myproducer.sendMsg(this.topic,sb);
//            如果队列为空等待1秒钟
                if (queue.isEmpty()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
//        myproducer.endProducer();
            util.endFtp(ftpClient);
        }
        else System.out.println("退出");
    }
}
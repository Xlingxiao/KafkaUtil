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
    private int retries;
    private BlockingQueue queue;
    private String topic;
    private Lock lock = new ReentrantLock();
    filePathConsumer(BlockingQueue queue,int retries, String topic) {
        this.queue = queue;
        this.retries = retries;
        this.topic = topic;
    }

    public void run() {
        System.out.printf("消费文件路径者 %s 启动\n",Thread.currentThread().getName());
//        刚开始时队列为空所有消费者都开始等待
        while(queue.size()<30){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        获取FTPClient对象
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null){
//            处理文件内容对象
            ProcessContent process = new ProcessContent();
//            发送处理过的文件内容的对象
            myProducer myproducer = new myProducer();
            StringBuilder stringBuilder;
            while (!queue.isEmpty()){
//            如果队列是空的不进行下面的操作
                lock.lock();
                if (queue.isEmpty()){
                    lock.unlock();
                    continue;
                }
                String path = (String) this.queue.poll();
                lock.unlock();
//                下载文件内容,指定文件下载失败后的重试次数
                stringBuilder = util.getDownload(ftpClient,path,retries);
                if (stringBuilder==null||stringBuilder.length()<1) continue;
//                对文件内容进行处理
                stringBuilder = process.startProcess(stringBuilder);
//                查看处理后的文件第一个“，”之前的内容t
                System.out.println(stringBuilder.toString().split(",",2)[0]);
//                发送处理过的内容
                myproducer.sendMsg(this.topic,stringBuilder);
//                循环10次判断队列是否为空，确认为空后返回
                for (int i = 0; i < 5; i++) {
//                  如果队列为空等待2秒钟
                    if (queue.isEmpty()) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else break;
                }
            }
//            关闭FTP连接
            util.endFtp(ftpClient,Thread.currentThread().getName());
        }
        else System.out.println("退出");
        System.out.println(Thread.currentThread().getName()+"处理结束---------");
    }
}

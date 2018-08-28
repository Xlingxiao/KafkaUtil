package producer.FTP.FTPThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 根据生产者消费者方式创建
 * 生产者负责读取FTP服务其上文件的路径
 * 消费者负责根据路径下载文件，并发送到kafka
 */
public class FTPStart {
    public static void main(String[] args) {
        String topic = "webTopic";
        String initPath = "./code/jupyter";
        BlockingQueue queue = new ArrayBlockingQueue(1000);
        filePathProducer pathProducer = new filePathProducer(queue,initPath);
        filePathConsumer pathConsumer = new filePathConsumer(queue,topic);
        for (int i =0;i<2;i++){
            new Thread(pathProducer,"生产者-"+i).start();
        }
        for (int i =0;i<15;i++){
            new Thread(pathConsumer,"消费者-"+i).start();
        }
    }
}
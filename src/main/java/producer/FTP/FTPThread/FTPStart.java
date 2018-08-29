package producer.FTP.FTPThread;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 根据生产者消费者方式创建
 * 生产者负责读取FTP服务其上文件的路径
 * 消费者负责根据路径下载文件，并发送到kafka
 * **********注意***********
 * 消费者和生产者的总线程数量不能高于Ftp服务器设置的连接总数的一半
 * 高于连接总数创建出来也没什么用
 * 因为ftp服务器要用一个端口接收客户端的控制请求，
 * 另一个端口用于发送数据给客户端
 * 被动模式下FTP服务器上指定了一个端口范围超过这个端口范围的一半就会出问题
 */
public class FTPStart {
    public static void main(String[] args) {
//        指定发送的topic
        String topic = "webTopic";
//        指定FTP服务器上的文件初始文件夹
        String initPath = "./code/jupyter/tmp";
//        下载文件失败后的重试次数
        int retries = 3;
        ArrayBlockingQueue queue = new ArrayBlockingQueue(1000);
        filePathProducer pathProducer = new filePathProducer(queue,initPath);
        filePathConsumer pathConsumer = new filePathConsumer(queue,retries,topic);
        for (int i =0;i<2;i++){
            new Thread(pathProducer,"生产者-"+i).start();
        }
        for (int i =0;i<4;i++){
            new Thread(pathConsumer,"消费者-"+i).start();
        }
    }
}
package producer.FTP.FTPThread;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
//        指定发送的topic
    private static String topic;
//        指定FTP服务器上的文件初始文件夹
    private static String initPath;
//        下载文件失败后的重试次数
    private static int retries;
    private static int PathProducerNumber;
    private static int PathConsumerNumber;

    public static void main(String[] args) {
        initProperties();
        ArrayBlockingQueue queue = new ArrayBlockingQueue(1000);
        filePathProducer pathProducer = new filePathProducer(queue,initPath);
        filePathConsumer pathConsumer = new filePathConsumer(queue,retries,topic);
        for (int i =0;i<PathProducerNumber;i++){
            new Thread(pathProducer,"生产者-"+i).start();
        }
        for (int i =0;i<PathConsumerNumber;i++){
            new Thread(pathConsumer,"消费者-"+i).start();
        }
    }

    /**
     * 初始化程序所需要的配置项
     */
    private static void initProperties(){
        Properties props = new Properties();
        InputStream is = FTPStart.class.getClassLoader().getResourceAsStream("myInit.properties");
        try{
            props.load(is);
//        指定发送的topic
            topic = props.getProperty("topic");
//        指定FTP服务器上的文件初始文件夹
            initPath = props.getProperty("ftpInitPath");
//        下载文件失败后的重试次数
            retries = Integer.parseInt(props.getProperty("ftpFileRetries"));
            PathProducerNumber = Integer.parseInt(props.getProperty("ftpPathProducerNumber"));
            PathConsumerNumber = Integer.parseInt(props.getProperty("ftpPathConsumerNumber"));
        } catch (IOException e) {
            System.out.println("没有读取到配置文件，将使用默认设置");
            defaultProperties();
        } catch (Exception e){
            System.out.println("读取配置文件内容不匹配，将使用默认设置");
            defaultProperties();
        }
    }
    private static void defaultProperties(){
        topic = "webTopic";
        initPath = "./code/jupyter/tmp";
        retries = 3;
        PathProducerNumber = 2;
        PathConsumerNumber = 4;
    }
}
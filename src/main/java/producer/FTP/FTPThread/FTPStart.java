package producer.FTP.FTPThread;

import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
 *
 * 测试定时任务时修改了初始时消费者等待队列中对象数量的值
 * 目前还应该设置两个时间变量控制不重复读取同一文件
 */
public class FTPStart implements Runnable{
//        指定发送的topic
    private static String topic;
//        指定FTP服务器上的文件初始文件夹
    private static String initPath;
//        下载文件失败后的重试次数
    private static int retries;
//    生产文件路径者和消费文件路径者的线程数量
    private static int PathProducerNumber;
    private static int PathConsumerNumber;
//    测试时定时任务的次数
    private int times = 0;
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        FTPStart ftpStart = new FTPStart();
        executor.scheduleWithFixedDelay(ftpStart,0,10, TimeUnit.SECONDS);
        while (ftpStart.times<50){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        System.out.println(format.format(Calendar.getInstance().getTime()));
    }

    /**
     * 启动定时任务的主要方法
     */
    @Override
    public void run() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        System.out.println("任务开始\t"+format.format(Calendar.getInstance().getTime())+"\t"+times++);
        initProperties();
        ArrayBlockingQueue queue = new ArrayBlockingQueue(1000);
        filePathProducer pathProducer = new filePathProducer(queue,initPath);
        filePathConsumer pathConsumer = new filePathConsumer(queue,retries,topic);
        List<Thread> pList = new ArrayList<>();
        List<Thread> cList = new ArrayList<>();
        for (int i =0;i<PathProducerNumber;i++){
            Thread t = new Thread(pathProducer,"生产者-"+i);
            pList.add(t);
            t.start();
        }
        for (int i =0;i<PathConsumerNumber;i++){
            Thread t = new Thread(pathConsumer,"消费者-"+i);
            cList.add(t);
            t.start();
        }
        for (Thread t :
                pList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Thread t :
                cList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("任务执行完成 "+times+" 次 "+format.format(Calendar.getInstance().getTime()));
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

    /**
     * 默认FTP配置项
     */
    private static void defaultProperties(){
        topic = "webTopic";
        initPath = "./code/jupyter/tmp";
        retries = 3;
        PathProducerNumber = 2;
        PathConsumerNumber = 4;
    }
}
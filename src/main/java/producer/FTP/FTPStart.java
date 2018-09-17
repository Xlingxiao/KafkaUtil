package producer.FTP;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
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
 * <p>
 * 再consumer的地方设置了10s内没有生产到4个文件路径就退出，
 * 应该在producer处同样添加超时退出机制超时关闭线程
 */
public class FTPStart implements Runnable {
//    定时任务的间隔时间
    private int intervalsTime;
//    存储文件路径的队列大小
    private int queueLength;
    //        指定发送的topic
    private String topic;
    //        指定FTP服务器上的文件初始文件夹
    private String initPath;
    //        下载文件失败后的重试次数
    private int retries;
    //    生产文件路径者和消费文件路径者的线程数量
    private int PathProducerNumber;
    private int PathConsumerNumber;
    //    测试时定时任务的次数
    private int times = 0;
    //    程序上次执行完成时间
    private long lastTime;
    //    程序本次执行开始时间
    private long thisTime;

    public static void main(String[] args) {
//        用来执行定时任务
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
//        创建FTP服务对象
        FTPStart ftpStart = new FTPStart();
//        如果没有获得配置文件就退出
        if (ftpStart.initProperties()) {
            return;
        }
//        指定程序上次运行时间
        ftpStart.lastTime = 0;

//        开始执行定时任务
        executor.scheduleWithFixedDelay(ftpStart, 0, ftpStart.intervalsTime, TimeUnit.SECONDS);
    }

    /**
     * 启动定时任务的主要方法
     */
    @Override
    public void run() {
//        获取当前时间
        Calendar nowTime = Calendar.getInstance();
//        两种格式化的时间格式分别精确到分钟和秒
//        精确到分钟的文件是用来处理FTP文件的
//        因为Apache 的FTPClient包中下载的文件时间只精确到分钟级别所以这里
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        精确到秒的时间格式是用来输出的
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        获得本次程序运行时间 String
        String thisTimeString = format.format(nowTime.getTime());
        try {
//            将本次运行时间String转为Date
            Date newDate = format.parse(thisTimeString);
            thisTime = newDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("任务开始\t" + "\t" + times++);
        System.out.println("lastTime " + format2.format(new Date(lastTime)));
        System.out.println("thisTime " + format2.format(new Date(thisTime)));
//        if (!initProperties()) {
//            return;
//        }
//        装FTP文件路径的队列
        ArrayBlockingQueue queue = new ArrayBlockingQueue(queueLength);
        filePathProducer pathProducer = new filePathProducer(queue, initPath, lastTime, thisTime);
        filePathConsumer pathConsumer = new filePathConsumer(queue, retries, topic, PathConsumerNumber);
        List<Thread> pList = new ArrayList<>();
        List<Thread> cList = new ArrayList<>();

//        先创建文件路径生产者
        for (int i = 0; i < PathProducerNumber; i++) {
            Thread t = new Thread(pathProducer, "生产者-" + i);
            pList.add(t);
            t.start();
        }
        for (int i = 0; i < 8; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (queue.size() > PathConsumerNumber)
                break;
        }
//        在八秒钟内生产到指定的文件路径数量就启动consumer
        if (queue.size() > PathConsumerNumber) {
            for (int i = 0; i < PathConsumerNumber; i++) {
                Thread t = new Thread(pathConsumer, "消费者-" + i);
                cList.add(t);
                t.start();
            }
//            等到Producer 和 Consumer执行结束
            for (Thread t : pList) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Thread t : cList) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            将程序上次执行时间指定为本次程序运行时间
            lastTime = thisTime;
            System.out.println("任务执行完成 " + times + " 次 " + format.format(Calendar.getInstance().getTime()));
        } else {
            for (Thread pThread :
                    pList) {
//                在指定时间内没有生产到指定数据量我们就直接关闭这个线程
//                        这个地方急需优化，关闭线程使用的方式太过于危险
                pThread.stop();
            }
            System.out.println("生产者未能在指定时间内生产文件路径等待下次启动");
        }

    }

    /**
     * 初始化程序所需要的配置项
     */
    private boolean initProperties() {
        boolean flag = false;
        Properties props = new Properties();
        InputStream is = FTPStart.class.getClassLoader().getResourceAsStream("myInit.properties");
        try {
            props.load(is);
//        指定发送的topic
            topic = props.getProperty("topic");
//        指定FTP服务器上的文件初始文件夹
            initPath = props.getProperty("ftpInitPath");
//        下载文件失败后的重试次数
            retries = Integer.parseInt(props.getProperty("ftpFileRetries"));
            PathProducerNumber = Integer.parseInt(props.getProperty("ftpPathProducerNumber"));
            PathConsumerNumber = Integer.parseInt(props.getProperty("ftpPathConsumerNumber"));
            intervalsTime = Integer.parseInt(props.getProperty("intervalsTime"));
            queueLength = Integer.parseInt(props.getProperty("queueLength"));
            flag = true;
        } catch (IOException e) {
            System.out.println("没有读取到配置文件，将使用默认设置");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("读取配置文件内容不匹配，将使用默认设置");
            e.printStackTrace();
        }
        return flag;
    }

}
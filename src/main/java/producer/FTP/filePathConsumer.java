package producer.FTP;

import common.FTPUtil;
import common.myProducer;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
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
    private int pathConsumerNumber;
    private List<String> fileAttributes;

    filePathConsumer(BlockingQueue queue, int retries, String topic, int pathConsumerNumber) {
        this.queue = queue;
        this.retries = retries;
        this.topic = topic;
        this.pathConsumerNumber = pathConsumerNumber;
        initConfig();
    }

    public void run() {
//        System.out.printf("消费文件路径者 %s 启动\n",Thread.currentThread().getName());
//        刚开始时队列为空所有消费者都开始等待,至少等到队列中的对象数量等于consumer线程数再开始工作
        for (int i = 0; i < 10; i++) {
            if(queue.size()<pathConsumerNumber){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else break;
        }
//        获取FTPClient对象
        FTPUtil util = new FTPUtil();
        FTPClient ftpClient = util.getFtpClient();
        if (ftpClient!=null){
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

//                查看文件第一行
                System.out.println(stringBuilder.toString().split("\n",2)[0]);
                String[] strList = processFile(stringBuilder);
                for (String str : strList) {
//                发送处理过的内容
                    myproducer.sendMsg(this.topic,str);
                }
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
            util.endFtp(ftpClient, Thread.currentThread().getName());
        }
        else System.out.println("退出");
        System.out.println(Thread.currentThread().getName()+"处理结束---------");
    }

    /**
     * 处理FTP文件内容改为json数据
     */
    private String[] processFile(StringBuilder stringBuilder){
        StringBuilder tmpBuilder = new StringBuilder();
        String [] strings = stringBuilder.toString().split("\n");
        for (int i = 0; i < strings.length; i++) {
            String[] oneMsg = strings[i].split("\t");
            try {
                tmpBuilder.append("{");
                for (int j = 0; j < fileAttributes.size(); j++) {
                    tmpBuilder.append("\"");
                    tmpBuilder.append(fileAttributes.get(j));
                    tmpBuilder.append("\"");

                    tmpBuilder.append(":");

                    tmpBuilder.append("\"");
                    tmpBuilder.append(oneMsg[j]);
                    tmpBuilder.append("\",");
                }
                tmpBuilder.delete(tmpBuilder.length()-1,tmpBuilder.length());
                tmpBuilder.append("}");
            } catch (IndexOutOfBoundsException e){
                System.out.println("指定的文件解析字段超过了文件中真实字段，本条数据放弃处理");
                System.out.println(tmpBuilder.toString());
                continue;
            }finally {
                strings[i] = tmpBuilder.toString();
                tmpBuilder.delete(0,tmpBuilder.length());
            }
        }
        return strings;
    }

    /**
     * 初始化FTP文件内部的字段
     * 字段指定数量超过FTP文件中的字段将不会插入ES数据库
     * 字段指定数量低于FTP文件中的字段将只会插入指定字段的数据
     */
    private void initConfig() {
//        获取上下文文件
        InputStream is = filePathConsumer.class.getClassLoader().getResourceAsStream("FTP/fileField");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String content;
        fileAttributes = new LinkedList<>();
//        将文件加入到contexts中
        try {
            while (null != (content = br.readLine())) {
                if (content.contains("#"))
                    continue;
                fileAttributes.add(content);
            }
        } catch (IOException e){
            System.out.println("读取文件解析文件出错");
            e.printStackTrace();
        }
    }
}

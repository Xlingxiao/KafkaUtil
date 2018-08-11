package producer.FTP;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class SendOneFile {
    /**
     *@功能 实现producer发送一篇ftp文章
     *@描述
     * 1.getFtpClient()  获取ftp对象
     * 2.getMsg()  获取ftp只当路径下的一个文件内容
     * 3.ProcessContent()  解析文件内容
     * 4.getProducer()  获得一个Producer对象
     * 5.StartProducer()  将解析好的文件内容交给producer发送
     * 6.endProducer()  关闭producer
     *@创建人  Lingxiao
     *@创建时间
     */
    public static FTPClient getFtpClient() {
        //ftp服务器地址
        String url = "120.78.160.135";
        //ftp服务器端口号默认为21
        Integer port = 21 ;
        //ftp登录账号
        String username = "hadoop";
        //ftp登录密码
        String password = "123456";
//            本地路径
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.connect(url, port);
            ftp.login(username, password);//登录
//            获取链接状态码
            reply = ftp.getReplyCode();
//            验证链接状态码
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return null;
            }
            ftp.setControlEncoding("UTF-8");
//            nat网络环境下需要设置为被动模式
            ftp.enterLocalPassiveMode();
        } catch (SocketException e){
            System.out.println("连接ftp服务器出错");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftp;
    }


//    输入ftp对象、文件路径、文件名
//    输出StringBuilder对象
    private StringBuilder getMsg(FTPClient ftp,String remotePath,String fileName){
//        转移到FTP服务器目录
        StringBuilder sb = new StringBuilder();
        try {
            ftp.changeWorkingDirectory(remotePath);
//            得到工作路径下的所有文件
            FTPFile[] fs = ftp.listFiles();
            for(FTPFile ff:fs){
                if(ff.getName().equals(fileName)){
//                    判断是文件夹或者是文件
//                    isFile()  isDirectory()
                    if(ff.isDirectory()){
                        System.out.println(ff.getName()+"：是一个文件夹，不能直接下载");
                        return null;
                    }
//                    获取文件输入流
                    InputStream is = ftp.retrieveFileStream(fileName);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
//                    处理输入流的内容
                    sb = ProcessContent(br);
                    br.close();
//                    先关闭输入流再获取传输完成的状态码
//                    保证下一次还能继续获取
                    is.close();
                    ftp.completePendingCommand();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (ftp.isConnected()){
                try {
//                    ftp.disconnect();
                    ftp.logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb;
    }


    public static StringBuilder ProcessContent(BufferedReader br){
        String msg ;
        StringBuilder sb = new StringBuilder();
//      逐行读取到StringBulider中
        try{
            while ((msg=br.readLine())!= null) {
                sb.append(msg);
                sb.append("\r\n");
                msg=new String(msg.getBytes("iso-8859-1"),"UTF-8");
            }
        }catch (IOException e){
            sb = null;
            e.printStackTrace();
        }
        return sb;
    }


    public static Producer<String,String> getProducer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "120.78.160.135:9092");   //用于初始化kafka集群，中用逗号分隔
        props.put("acks", "all");    //当所有的follow都拷贝到消息后再进行确认
        props.put("retries", 2);     //发送重试次数
        props.put("batch.size", 16384);     //控制批量发送消息的大小，减少访问服务端的次数提高性能
        props.put("linger.ms", 500);          //控制消息每隔多少时间进行发送，减少访问服务端次数
        props.put("buffer.memory", 33554432);   //设置换冲区大小，发送速度大于这个值的话会导致缓冲被耗尽
        //设置键，值以什么序列的形式进行发送
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //生产者发送消息
        String topic = "test";
        Producer<String, String> procuder = new KafkaProducer<String,String>(props);
        return procuder;
    }


    public static void StartProducer(Producer<String,String> producer,String topic, StringBuilder sb){
        String value = sb.toString();
//        开始传输
        ProducerRecord<String, String> msg = new ProducerRecord<String, String>(topic, value);
        producer.send(msg);
    }


    public static void endProducer(Producer<String,String> producer,String topic){
        //列出topic的相关信息
        List<PartitionInfo> partitions = new ArrayList<PartitionInfo>() ;
        partitions = producer.partitionsFor(topic);
        for(PartitionInfo p:partitions)
        {
            System.out.println(p);
        }
        System.out.println("send message over.");
        //        设置producer的消息在10000ms内没有发出就强制关闭
//        producer.close(10000,TimeUnit.MILLISECONDS);
        producer.close();
    }

    @Test
    public void testUpLoadFromString(){
        String topic = "test";
        String remotePath = "./code/jupyter/tmp/haerbing/";
        String fileName = "1.txt";
        FTPClient ftpClient = getFtpClient();
        if (ftpClient!=null){
            StringBuilder sb = getMsg(ftpClient,remotePath,fileName);
            if (sb!=null){
                Producer<String,String> producer = getProducer();
                try{
                    StartProducer(producer,topic,sb);
                    endProducer(producer,topic);
                }catch (KafkaException e){
                    System.out.println("Kafka Producer 发送消息失败");
                }
            }

        }
        else{
            System.out.println("获取ftp对象失败");
        }
        System.out.println("程序执行完毕！");
    }
}

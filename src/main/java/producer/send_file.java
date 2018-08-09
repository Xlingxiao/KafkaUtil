package producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class send_file {
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");   //用于初始化kafka集群，中用逗号分隔
        props.put("acks", "all");    //当所有的follow都拷贝到消息后再进行确认
        props.put("retries", 0);     //发送重试次数
        props.put("batch.size", 16384);     //控制批量发送消息的大小，减少访问服务端的次数提高性能
        props.put("linger.ms", 1000);          //控制消息每隔多少时间进行发送，减少访问服务端次数
        props.put("buffer.memory", 33554432);   //设置换冲区大小，发送速度大于这个值的话会导致缓冲被耗尽
        //设置键，值以什么序列的形式进行发送
        props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        //生产者发送消息
        String topic = "filetopic";
        Producer<byte[], byte[]> procuder = new KafkaProducer<byte[], byte[]>(props);
        //思路：先把图片读入到内存--》写入某个文件
        //因为是二进制文件，因此只能用字节流完成
        //输入流
        FileInputStream fis=null;

        //输出流
        try {
            fis=new FileInputStream("e:/tmp/a.png");
//            fos=new FileOutputStream("d:/b.png");
            byte bytes[]=new byte[2048];

            int n=0;  //记录实际读取到的字节数
            while((n=fis.read(bytes))!=-1)
            {
                ProducerRecord<byte[], byte[]> msg = new ProducerRecord<byte[], byte[]>(topic, bytes);
                procuder.send(msg);
                //输出到指定文件
//                fos.write(bytes);
                System.out.println(msg);
            }
            ProducerRecord<byte[], byte[]> msg = new ProducerRecord<byte[], byte[]>(topic, bytes);
            procuder.send(msg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        finally{
            //关闭流
            try {
                fis.close();
                procuder.close(100, TimeUnit.MILLISECONDS);
//                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

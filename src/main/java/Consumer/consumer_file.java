package Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class consumer_file {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();    //创建容器
        props.put("bootstrap.servers", "172.16.2.24:9092");      //初始化kafka集群，配置服务器
        props.put("group.id", "0");         //consumer启动时配置文件中定义的groupid
        props.put("enable.auto.commit", "true"); //定期提交consumer的偏移量
        props.put("compression.type","snappy");
        props.put("fetch.message.max.bytes","7340032");
        props.put("session.timeout.ms", "30000");
        props.put("auto.commit.interval.ms", "1000");  //enable.auto.commit为true时使用，以毫秒为单位提交偏移量给kafka
        //设置键，值以什么序列格式进行传送
        props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        //创建消费者
        KafkaConsumer<byte[],byte[]> consumer = new KafkaConsumer<byte[],byte[]>(props);
        //填写话题名
        consumer.subscribe(Arrays.asList("filetopic"));
        //设置输出文件名以及地址
        Date date =new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH-mm-ss");
        //创建文件输出流
        FileOutputStream fos=null;
        fos=new FileOutputStream("e:/tmp/"+df.format(date).toString()+".png");
        while (true) {
            ConsumerRecords<byte[],byte[]> records = consumer.poll(500);
            for (ConsumerRecord<byte[],byte[]> record : records){
                //将文件进行写出
                fos.write(record.value());
//                System.out.println("value: " + record.value());
                //输出收到的信息
                System.out.println(record.toString());
            }
        }
    }
}

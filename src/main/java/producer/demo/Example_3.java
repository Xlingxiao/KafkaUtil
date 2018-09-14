package producer.demo;

import common.MySerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: KafkaUtil
 * @description: 测试自定义序列化
 * @author: Ling
 * @create: 2018/09/13 18:56
 **/
public class Example_3 {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.16.2.23:9092");   //用于初始化kafka集群，中用逗号分隔
        props.put("acks", "all");    //当所有的follow都拷贝到消息后再进行确认
        props.put("retries", 3);     //发送重试次数
        props.put("batch.size", 16384);     //控制批量发送消息的大小，减少访问服务端的次数提高性能
        props.put("linger.ms", 1000);          //控制消息每隔多少时间进行发送，减少访问服务端次数
        props.put("buffer.memory", 33554432);   //设置换冲区大小，发送速度大于这个值的话会导致缓冲被耗尽
        //设置键，值以什么序列的形式进行发送
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", MySerializer.class.getName());
        //生产者发送消息
        Producer<String, Object> producer = new KafkaProducer<>(props);
        String topic = "test";
        Map<String,String> map = new HashMap<>();
        map.put("1", "test1");
        map.put("2", "test2");
        map.put("3", "test33");

        ProducerRecord<String,Object> msg = new ProducerRecord<>(topic,map);
        producer.send(msg);
        //列出topic的相关信息
        List<PartitionInfo> partitions;
        partitions = producer.partitionsFor(topic);
        for(PartitionInfo p:partitions)
        {
            System.out.println(p);
        }

        System.out.println("send message over.");
//        设置producer的消息在10000ms内没有发出就强制关闭
        producer.close(10000, TimeUnit.MILLISECONDS);
    }
}

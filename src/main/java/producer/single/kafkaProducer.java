package producer.single;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

/**
 * @创建人
 * @创建时间
 * @描述
 */
public class kafkaProducer {

    public kafkaProducer() {

    }
    private Producer<String,String> Producer() {
        System.out.printf("======================");
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
        Producer<String, String> procuder = new KafkaProducer<>(props);
        return procuder;
    }

    private static class createProducer {
//        第一次调用到这个类之后掉头用相应方法返回producer是对象
//        第二次调用到时直接返回这个对象
        private final static Producer<String,String> producer = new kafkaProducer().Producer();
    }
//    静态方法在不创建类的前提下也可以使用,并且不创建该类
    public static Producer<String,String> getProducer(){
        System.out.println("++++++++++++++++++++");
        return createProducer.producer;
    }
}

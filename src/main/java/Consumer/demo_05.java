package Consumer;

import common.MyDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * @program: KafkaUtil
 * @description: 测试自定义反序列化对象
 * @author: Ling
 * @create: 2018/09/13 19:07
 **/
public class demo_05 {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.16.2.23:9092");
        props.put("group.id", "0");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", MyDeserializer.class.getName());
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        String topic = "test";
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));
            Map<String,String> msg;
            for (ConsumerRecord<String, Object> record : records){
//                System.out.printf("offset = %d, value = %s%n", record.offset(), record.value());
                msg = (Map<String, String>) record.value();
                System.out.println(msg.get("1"));
                System.out.println(msg.get("2"));
                System.out.println(msg.get("3"));
            }

        }
    }
}

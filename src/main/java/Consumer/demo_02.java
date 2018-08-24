package Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @program: KafkaUtil
 * @description: 手动配置partition和topic，可指定接受消息的位置
 * @author: Ling
 * @create: 2018/08/24 17:17
 **/
public class demo_02 {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.16.2.24:9092");
        props.put("group.id", "1");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        String topic = "test2";
//        指定topic 自动分配partition
//        consumer.subscribe(Arrays.asList(topic));
//        手动指定partition和topic
        List<TopicPartition> partitions = new ArrayList<>();
        partitions.add(new TopicPartition(topic,0));
        partitions.add(new TopicPartition(topic,1));
        consumer.assign(partitions);
        consumer.seekToBeginning(partitions);
//        consumer.seekToEnd(partitions);
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records){
                System.out.printf("offset = %d, value = %s%n", record.offset(), record.value());
            }

        }
    }
}

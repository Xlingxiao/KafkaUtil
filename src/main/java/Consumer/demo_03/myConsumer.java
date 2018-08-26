package Consumer.demo_03;/**
 * @创建人
 * @创建时间
 * @描述
 */

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * @program: KafkaUtil
 * @description: 单独的consumer通过设置不同的partition获取数据相互不影响
 * @author: Ling
 * @create: 2018/08/26 15:17
 **/
public class myConsumer implements Runnable {

    int id;
    String topic;
    int partition;

    private KafkaConsumer<String,String> consumer;
    public myConsumer(int id ,String topic, int partition) {
        this.id = id;
        this.partition = partition;
        this.topic = topic;
    }

    @Override
    public void run() {
        createConsumer();
        startReceive();
    }

    private void createConsumer(){
        Properties props = new Properties();
        InputStream is = myConsumer.class.getClassLoader().getResourceAsStream("consumer.properties");
        try {
            props.load(new InputStreamReader(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
        consumer = new KafkaConsumer<>(props);
        consumer.assign(Arrays.asList(new TopicPartition(topic,partition)));
    }

    private void startReceive(){
        ConsumerRecords<String,String> records;
        while (true){
            records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord record : records){
                System.out.printf("Thread-%d, offset = %d, value = %s%n",this.id ,record.offset(), record.value());
            }
        }

    }
}

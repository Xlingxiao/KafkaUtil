package Consumer.demo04;

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
 * @description: 获取kafka服务器消息的对象
 * @author: Ling
 * @create: 2018/09/11 17:47
 **/
public class MyWorker implements Runnable{
    /**
     * id 每个消费者线程的id
     * topic 消费者消费的topic
     * partition 指定消费者消费的partition
     */
    int id;
    String topic;
    int partition;
    private KafkaConsumer<String,String> consumer;

    public MyWorker(int id ,String topic, int partition) {
        this.id = id;
        this.partition = partition;
        this.topic = topic;
    }

    public void run() {
        createConsumer();
        startReceive();
    }

//        创建kafka consumer
    private void createConsumer(){
//        获取配置文件
        Properties props = new Properties();
        InputStream is = MyWorker.class.getClassLoader().getResourceAsStream("consumer.properties");
        try {
            props.load(new InputStreamReader(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        使用配置文件创建consumer
        consumer = new KafkaConsumer<>(props);
//        指定consumer消费的分区
//        在指定分区时分区的内容已经包括了topic所以指定分区进行消费就ok
        consumer.assign(Arrays.asList(new TopicPartition(topic,partition)));
    }

    //        开始消费
    private void startReceive(){
//        声明records对象,kafka的消息使用这个对象进行读取
        ConsumerRecords<String,String> records;
        while (true){
//            每1000ms从集群中获取一次数据
            records = consumer.poll(Duration.ofMillis(1000));
//            一个records中可能包含多条消息遍历这些消息
            for (ConsumerRecord record : records){
                System.out.printf("Thread-%d, offset = %d, value = %s%n",this.id ,record.offset(), record.value());
            }
        }

    }
}

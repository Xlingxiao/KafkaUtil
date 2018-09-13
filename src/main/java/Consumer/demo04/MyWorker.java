package Consumer.demo04;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import common.ESUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
    private String topic;
    private int partition;
    private KafkaConsumer<String,String> consumer;
    private ESUtil esUtil;
    private String Index;
    private String Type;

    MyWorker( int partition) {
        this.partition = partition;
        initProperties();
    }

//    初始化服务器配置
    private void initProperties(){
        Properties props = new Properties();
        InputStream is = Start.class.getClassLoader().getResourceAsStream("myInit.properties");
        try {
            props.load(is);
//        指定consumer消费的topic
            topic = props.getProperty("topic");
            String ESIP = props.getProperty("ESIP");
            esUtil = new ESUtil(ESIP);
            Index = props.getProperty("index");
            Type = props.getProperty("type");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        consumer = createConsumer();
//        指定consumer消费的分区
//        在指定分区时分区的内容已经包括了topic所以指定分区进行消费就ok
        consumer.assign(Arrays.asList(new TopicPartition(topic,partition)));
        startReceive();
    }

//        开始消费
    private void startReceive(){
//        声明records对象,kafka的消息使用这个对象进行读取
        ConsumerRecords<String,String> records;
        Gson gson = new Gson();
        Map<String,?> msg = new HashMap<>();
        while (true){
//            每5000ms从集群中获取一次数据
            records = consumer.poll(Duration.ofMillis(1000));
//            一个records中可能包含多条消息遍历这些消息
            for (ConsumerRecord record : records){
                try{
                    msg = gson.fromJson(record.value()+"",msg.getClass());
                    esUtil.insertOneData(Index,Type,msg);
                    consumer.commitSync();
                } catch (JsonSyntaxException e){
                    System.out.println("发送过来的不是json数据自动忽略");
                    consumer.commitSync();
                }
            }
        }

    }

    //        创建kafka consumer
    public static KafkaConsumer<String,String> createConsumer(){
//        获取配置文件
        Properties props = new Properties();
        InputStream is = MyWorker.class.getClassLoader().getResourceAsStream("kafka/ControlOffsetConsumer.properties");
        try {
            props.load(new InputStreamReader(is));
        } catch (IOException e) {
            System.out.println("获取配置文件失败");
            e.printStackTrace();
        }
//        使用配置文件创建consumer
        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }
}

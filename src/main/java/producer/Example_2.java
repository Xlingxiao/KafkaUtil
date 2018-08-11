package producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

//kafka官方例子2

public class Example_2 {
    public static void main(String args[]){
        Properties props = new Properties();
        props.put("bootstrap.servers", "120.78.160.135:9092");
        props.put("transactional.id", "my-transactional-id");
        Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
        producer.initTransactions();

        try {
            producer.beginTransaction();
            for (int i = 0; i < 10; i++)
                producer.send(new ProducerRecord<>("test", Integer.toString(i), Integer.toString(i)));
            producer.commitTransaction();
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            //我们无法从这些例外中恢复，因此我们唯一的选择是关闭生产者并退出。
            producer.close();
        } catch (KafkaException e) {
            // 对于所有其他例外情况，只需中止交易并重试.
            producer.abortTransaction();
        }
        producer.close();
    }
}

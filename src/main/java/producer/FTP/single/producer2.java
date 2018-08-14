package producer.FTP.single;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @创建人
 * @创建时间
 * @描述
 */
public class producer2 {
    private producer2() {
    }
    private Producer<String,String> createProducer() {
        Properties props = new Properties();
        InputStream is = producer2.class.getClassLoader().getResourceAsStream("producer.properties");
        try {
            props.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Producer<String, String> procuder = new KafkaProducer<String,String>(props);
        return procuder;
    }
    private static class create{
        private static final Producer<String,String> producer = new producer2().createProducer();
    }

    public static Producer<String,String> getProducer(){
        return create.producer;
    }
}

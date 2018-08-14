package producer.FTP.FTPThread;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class myProducer {
    private myProducer(){

    }
    private Producer<String,String> createMyProducer(){
        Properties props = new Properties();
        InputStream is = myProducer.class.getClassLoader().getResourceAsStream("producer.properties");
        try {
            props.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Producer<String,String> producer = new KafkaProducer<String, String>(props);
        return producer;
    }
    private static class broker{
        private static final Producer<String,String> inProducer = new myProducer().createMyProducer();
    }

    public static Producer<String,String> getProducer(){
        return broker.inProducer;
    }

    public static void endProducer(Producer<String,String> producer){
        producer.close();
        System.out.println("成功关闭Producer");
    }
}

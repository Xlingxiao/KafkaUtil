package producer.FTP.FTPThread;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class myProducer {
    public myProducer(){

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

    public Producer<String,String> getProducer(){
        return broker.inProducer;
    }

    public void sendMsg(String topic, StringBuilder sb){
        ProducerRecord<String, String> msg = new ProducerRecord<String, String>(topic, sb.toString());
        try {
            broker.inProducer.send(msg);
        }catch (IllegalStateException e){
            System.out.println("producer对象已经关闭");
        }
    }

    public void endProducer(){
        broker.inProducer.close(10, TimeUnit.SECONDS);
        System.out.println("成功关闭Producer");
    }
}

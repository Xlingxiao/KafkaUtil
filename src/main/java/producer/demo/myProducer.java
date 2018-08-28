package producer.demo;


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

    /**
     * 真正创建producer对象的方法
     * @return Producer<String,String>
     */
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

//    内部类用于保证创建单例的producer
    private static class broker{
        private static final Producer<String,String> inProducer = new myProducer().createMyProducer();
    }
//    返回一个producer对象
    public Producer<String,String> getProducer(){
        return broker.inProducer;
    }

    /**
     * 将sb对象转为string后发送到kafka topic
     * kafka producer对象不存在时会默认创建一个
     * 对象存在时会直接使用创建好的对象
     * @param topic
     * @param sb
     */
    public void sendMsg(String topic, StringBuilder sb){
        ProducerRecord<String, String> msg = new ProducerRecord<String, String>(topic, sb.toString());
        try {
            broker.inProducer.send(msg);
        }catch (IllegalStateException e){
            System.out.println("producer对象已经关闭");
        }
    }

    /**
     * 结束producer
     */
    public void endProducer(){
        broker.inProducer.close(10, TimeUnit.SECONDS);
        System.out.println("成功关闭Producer");
    }
}

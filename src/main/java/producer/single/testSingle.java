package producer.single;

import org.apache.kafka.clients.producer.Producer;

import static producer.single.kafkaProducer.getProducer;


/**
 * @创建人
 * @创建时间
 * @描述
 */
public class testSingle {
    public static void main(String[] args) {
        for (int i =0;i<100;i++){
            testThread test = new testThread();
            Thread thread = new Thread(test);
            thread.start();
        }
    }
}

class testThread implements Runnable{

    @Override
    public void run() {
        for (int i =0;i<100;i++){
            Producer<String,String> producer = getProducer();
        }
    }
}

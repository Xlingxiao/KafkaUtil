package producer.single;

import org.apache.kafka.clients.producer.Producer;

import static producer.single.producer2.getProducer;

/**
 * @创建人
 * @创建时间
 * @描述
 */
public class thread2 {
    public static void main(String[] args) {
        for (int i =0 ;i<200;i++){
            myThread thread = new myThread();
            Thread t = new Thread(thread);
            t.start();
        }
    }
}
class myThread implements Runnable{
    @Override
    public void run() {
        for (int i =0 ;i<200;i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Producer<String,String> producer = getProducer();
        }
    }
}

package Consumer.demo_03;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: KafkaUtil
 * @description: 通过多线程获取数据，每个线程从一个分区进行获取数据
 * @author: Ling
 * @create: 2018/08/26 15:08
 **/
public class demo_03 {
    public static void main(String[] args) throws InterruptedException {
        String topic ="webTopic";
        List<Thread> consumers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Thread consumer = new Thread(new myConsumer(i,topic,i));
            consumers.add(consumer);
            consumer.start();
        }
    }
}

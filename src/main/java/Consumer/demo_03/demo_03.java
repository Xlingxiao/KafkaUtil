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
    public static void main(String[] args){
//        指定consumer消费的topic
        String topic ="webTopic";
        int threadNum = 8;
        List<Thread> consumers = new ArrayList<>();
//        根据分区的数量创建consumer的线程数保证一个分区一个consumer
//        后期应该加入自动识别topic中有几个分区
//        由此来指定consumer 的数量
        for (int i = 0; i < threadNum; i++) {
            Thread consumer = new Thread(new myConsumer(i,topic,i));
            consumers.add(consumer);
            consumer.start();
        }
    }
}

package Consumer.demo04;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: KafkaUtil
 * @description: 使用手动提交的方式处理数据
 * @author: Ling
 * @create: 2018/09/11 17:46
 **/
public class Consumer {
    public static void main(String[] args){
//        指定consumer消费的topic
        String topic ="webTopic";
        int threadNum = 8;
        List<Thread> consumers = new ArrayList<>();
//        根据分区的数量创建consumer的线程数保证一个分区一个consumer
//        后期应该加入自动识别topic中有几个分区
//        由此来指定consumer 的数量
        for (int i = 0; i < threadNum; i++) {
            Thread consumer = new Thread(new MyWorker(i,topic,i));
            consumers.add(consumer);
            consumer.start();
        }
    }
}

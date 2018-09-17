package Consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static Consumer.MyWorker.createConsumer;


/**
 * @program: KafkaUtil
 * @description: 两个功能，
 *  1：自动根据topic中的分区数量创建线程，每个线程读取响应分区的内容
 *  2：手动提交Offset 处理完一条消息后才提交这条消息的offset，如果在处理消息途中出现错误下次启动时还能获取数据
 * @author: Ling
 * @create: 2018/09/11 17:46
 **/
public class Start {

    private static String topic;
    private static int partitionsNumber;

    public static void main(String[] args){
//        初始化指定的topic并设置相应topic的Partition Number
        if (! initProperties()){
            return;
        }
//        启动每个线程
        List<Thread> consumers = new ArrayList<>();
//        根据分区的数量创建consumer的线程数保证一个分区一个consumer
//        后期应该加入自动识别topic中有几个分区
//        由此来指定consumer 的数量
        for (int i = 0; i < partitionsNumber; i++) {
            Thread consumer = new Thread(new MyWorker(i));
            consumers.add(consumer);
            consumer.start();
        }
    }

//    初始化服务器配置
    private static boolean initProperties(){
        boolean flag = false;
        Properties props = new Properties();
        InputStream is = Start.class.getClassLoader().getResourceAsStream("myInit.properties");
        try {
            props.load(is);
//        动态获取分区数量先创建一个consumer使用partitionsFor方法获得分区数量
            KafkaConsumer tmpConsumer = createConsumer();
            topic = props.getProperty("topic");
            partitionsNumber = tmpConsumer.partitionsFor(topic).size();
            System.out.printf("topic %s 下有 %d 个分区\n",topic,partitionsNumber);
            tmpConsumer.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }
}

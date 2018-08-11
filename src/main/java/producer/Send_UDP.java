package producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Properties;

public class Send_UDP {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");   //用于初始化kafka集群，中用逗号分隔
        props.put("acks", "all");    //当所有的follow都拷贝到消息后再进行确认
        props.put("retries", 0);     //发送重试次数
        props.put("batch.size", 16384);     //控制批量发送消息的大小，减少访问服务端的次数提高性能
        props.put("linger.ms", 1);          //控制消息每隔多少时间进行发送，减少访问服务端次数
        props.put("buffer.memory", 33554432);   //设置换冲区大小，发送速度大于这个值的话会导致缓冲被耗尽
        //设置键，值以什么序列的形式进行发送
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //生产者需要发送到的话题
        String topic = "UDP_Topic";
//        创建生产者
        Producer<String,String> procuder = new KafkaProducer<String,String>(props);
        String value = null;
        while(true){
            value = "value_" + UdpServer();
            System.out.println(value);
            ProducerRecord<String,String> msg = new ProducerRecord<String,String>(topic, value);
            procuder.send(msg);
        }
    }
    static String UdpServer() throws IOException {
//           准备数据
//           创建UDP服务器端
        DatagramSocket server = new DatagramSocket(8989);
//           创建接收容器
        byte[] redata = new byte[1024];
//        创建接收包，定义每次的接收大小
        DatagramPacket packet = new DatagramPacket(redata, redata.length);

//        开始接收数据，数据存储在packet中
        server.receive(packet);
//            对接收到的数据实时发送到kafka
        byte[] data = packet.getData();
        String str = new String(data);
        server.close();
        return str;
    }
}

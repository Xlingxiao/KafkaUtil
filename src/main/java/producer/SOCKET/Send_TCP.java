package producer.SOCKET;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Send_TCP {
    public static void main(String[] args) throws IOException {
        int port = 8989;
        ServerSocket server = new ServerSocket(port);
        System.out.println("等待与客户端建立连接...");
        while(true){
            Socket socket = server.accept();
            new Thread(new Task(socket)).start();

        }
    }

}



class Task implements Runnable {

    private Socket socket;

    /**
     * 构造函数
     */
    public Task(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Properties props = Conf();
            String topic = "UDP_Topic";
            Producer<String, String> procuder = new KafkaProducer<String, String>(props);
            String value = null;
            value = handlerSocket();
            System.out.println(value);
            ProducerRecord<String, String> msg = new ProducerRecord<String, String>(topic, value);
            procuder.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跟客户端Socket进行通信
     *
     * @throws IOException
     */
    private String handlerSocket() throws Exception {
        // 跟客户端建立好连接之后，我们就可以获取socket的InputStream，并从中读取客户端发过来的信息了

        BufferedReader br = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String temp;
        int index;
        while ((temp = br.readLine()) != null) {
            if ((index = temp.indexOf("eof")) != -1) { // 遇到eof时就结束接收
                sb.append(temp.substring(0, index));
                break;
            }
            sb.append(temp);
        }
//        System.out.println("Form Cliect[port:" + socket.getPort()
//                + "] 消息内容:" + sb.toString());
        String st = sb.toString()+"Port： "+socket.getPort();
        // 回应一下客户端
        Writer writer = new OutputStreamWriter(socket.getOutputStream(),
                "UTF-8");
        writer.write(String.format("Hi,%d.天朗气清，惠风和畅！", socket.getPort()));
        writer.flush();
        writer.close();
        System.out.println(
                "To Cliect[port:" + socket.getPort() + "] 回复客户端的消息发送成功");
//
        br.close();
        socket.close();
        return st;
    }
    //    配置生产者属性的方法
    static Properties Conf(){
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
        return props;
    }
}
package producer.SOCKET.newVersion;

import common.myProducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * @program: KafkaUtil
 * @description: 主要工作对象，目前的设计是，客户端一直在读取数据，不间断，
 *              应该改为客户端没有数据过来时才检测客户端的心跳
 * @author: Ling
 * @create: 2018/09/10 10:22
 **/
public class workMan implements Runnable{

//    客户端对象
    private Socket socket;
//    客户端心脏对象
    private heartbeatDog heartbeat;
//    发送的kafka topic
    private String topic;
//    kafka Producer
    private myProducer producer;
//    收到的消息暂存地，消息量到达一定程度后进行发送
    private StringBuilder stringBuilder = new StringBuilder();

    workMan(Socket socket, String topic,heartbeatDog heartbeat) {
        this.socket = socket;
        this.heartbeat = heartbeat;
        this.topic = topic;
    }

    /**
     * 主要逻辑：
     * 1.根据客户端心脏判断客户端是否活着
     * 2.客户端活着就接收消息
     * 3.客户端客户端死掉就不接收消息
     *   但现在有一个问题，客户端死掉之后
     *   在读取的这边本身就会报异常，似乎
     *   没有必要弄这个心跳机制,
     */
    @Override
    public void run() {
        InputStream is;
        BufferedReader br ;
        try {
            is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String msg;
//            根据心跳标志判断客户端是否活着
            while (heartbeat.isFlag()||is.available()>0){
//                如果客户端有消息过来正常处理
                if (is.available()>0){
                    msg = br.readLine();
                    stringBuilder.append(msg);
//                获取到的内容差不多了进行处理或者直接发送给kafka
                    if (stringBuilder.length()>=10) {
                        System.out.println(stringBuilder.toString());
                        sendToKafka();
                        stringBuilder.delete(0,stringBuilder.length());
                    }
//                  客户端没有消息过来，休息一段时间再看
                } else{
                    Thread.sleep(2000);
                }
            }
            System.out.printf("%s 检测不到客户端心跳准备断开连接\n",Thread.currentThread().getName());
        } catch (SocketException e){
            System.out.printf("客户端 %s 主动断开连接\n",socket.getInetAddress());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        } finally {
            try {
                if(!socket.isClosed()){
                    socket.close();
                    System.out.printf("%s 关闭与客户端连接\n",Thread.currentThread().getName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 将消息丢给producer
     */
    private void sendToKafka(){
        if (producer==null)
            producer = new myProducer();
        producer.sendMsg(topic,stringBuilder);
    }
}

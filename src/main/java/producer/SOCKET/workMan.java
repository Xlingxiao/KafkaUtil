package producer.SOCKET;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import common.myProducer;

/** 
* @Description: 主要工作对象，在这里设置发送到kafka的topic
* @Author: Ling
* @Date: 2018/8/28 
*/
public class workMan implements Runnable{
    
    private Socket socket;
    private volatile boolean flag;
    private String topic;
    private myProducer producer;
    private StringBuilder stringBuilder = new StringBuilder();

    workMan(Socket socket, String topic) {

        this.socket = socket;
        this.topic = topic;
    }

    /**
     *@描述 连接保持不断，client发送的消息数量大于一定值时发送给kafka
     *      清空stringBuilder
     *      目前设计应该是定时2-3s接收客户端的心跳，记录上次心跳的时间；
     *      没有心跳之后关闭与客户端的连接
     *      等待一天后没有客户端的消息发过来就断掉连接
     *@参数  []
     *@返回值  void
     *@创建人  Lingxiao
     *@创建时间  2018/9/9
     */
    @Override
    public void run() {
        try {
            socket.setSoTimeout(200);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        BufferedReader br ;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while (!(msg=br.readLine()).equals("Goodbye")){
                if (msg.equals("Goodbye"))
                    break;
                else if(msg.equals("heartbeat"))
                    continue;

//                msg = msg.replaceAll("\\s*","");
                stringBuilder.append(msg);
//                获取到的内容差不多了进行处理或者直接发送给kafka
                if (stringBuilder.length()>=20) {
                    System.out.println(stringBuilder.toString());
//                    sendToKafka();
                    stringBuilder.delete(0,stringBuilder.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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

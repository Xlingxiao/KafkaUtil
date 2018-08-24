package producer.SOCKET;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;


public class workMan implements Runnable{

    private Socket socket;
    private String topic = "test";
    private myProducer producer;
    private StringBuilder sb = new StringBuilder();

    public workMan(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            handlerSocket();
            sendToKafka();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void handlerSocket() throws Exception {
        // 跟客户端建立好连接之后，我们就可以获取socket的InputStream，并从中读取客户端发过来的信息了

        BufferedReader br = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));
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
//        关闭与客户端的链接
        socket.close();
    }
//    发送消息
    private void sendToKafka(){
        if (producer==null)
            producer = new myProducer();
        producer.sendMsg(topic,sb);
    }
}

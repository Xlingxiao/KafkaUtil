package producer.SOCKET;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
* @Description: 创建socket服务端接收客户端请求
* @Author: Ling
* @Date: 2018/8/28
*/
public class TCP_Server {
//    服务端占用的本地端口
    private static int port;
//    服务端
    private static ServerSocket server;

//        客户端连接池
    private static int clientPoolNum;
//        生产者发送的topic
    private static String topic;
//    创建Socket服务端
    public static void main(String[] args) throws IOException {
        initProperties();
        server = new ServerSocket(port);
        System.out.printf("Socket 服务端已经启动，占用本地端口：%d\n",server.getLocalPort());
        start();
    }
//    启动服务端
    private static void start(){
        ExecutorService clientPool = Executors.newFixedThreadPool(clientPoolNum);
        Socket client ;
        while (true){
            try {
                client = server.accept();
                clientPool.submit(new workMan(client, topic));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initProperties(){
        Properties props = new Properties();
        InputStream is = TCP_Server.class.getClassLoader().getResourceAsStream("myInit.properties");
        try {
            props.load(is);
            port = Integer.parseInt(props.getProperty("socketServerPort"));
            clientPoolNum = Integer.parseInt(props.getProperty("socketClientPoolNum"));
            topic = props.getProperty("topic");
        } catch (IOException e) {
            System.out.println("读取配置文件失败，将使用默认设置启动服务");
            defaultProperties();
        } catch (Exception e){
            System.out.println("读取配置文件内容有误，将使用默认设置启动服务");
            defaultProperties();
        }
    }

    private static void defaultProperties(){
        port = 8888;
        clientPoolNum = 20;
        topic = "webTopic";
    }
}

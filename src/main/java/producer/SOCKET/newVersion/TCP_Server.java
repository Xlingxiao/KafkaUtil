package producer.SOCKET.newVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: KafkaUtil
 * @description: 使用两个服务器端口，一个用于检测心跳另一个用于接收消息
 * 使用两个服务端端口优点是互不干扰，缺点是可能导致服务器性能降低
 * @author: Ling
 * @create: 2018/09/10 10:20
 **/

public class TCP_Server {
//    服务端占用的本地端口
    private static int port;
//    服务端
    private static ServerSocket server;
//    心跳服务端
    private static ServerSocket heartbeatServer;
//    客户端连接池
    private static int clientPoolNum;
//    生产者发送的topic
    private static String topic;
//    创建Socket服务端

    public static void main(String[] args) throws IOException {
//        初始化服务端架设位置
        initProperties();
//        创建服务端
        server = new ServerSocket(port);
//        创建心跳检测服务
        heartbeatServer = new ServerSocket(port+1);
        System.out.printf("Socket 服务端已经启动，占用本地端口：%d\n",server.getLocalPort());
        System.out.printf("心跳服务端以启动，占用本地端口：%d\n",heartbeatServer.getLocalPort());
//        启动服务
        start();
    }

//    启动服务端
    private static void start(){
//        创建客户端连接池 和 心跳连接池
        ExecutorService clientPool = Executors.newFixedThreadPool(clientPoolNum);
        ExecutorService heartbeatClients = Executors.newFixedThreadPool(clientPoolNum);
        Socket client ;
        Socket heartbeatClient;
        while (true){
            try {
//                要求同时获取到客户端连接+客户端心脏才能启动服务
                client = server.accept();
                heartbeatClient = heartbeatServer.accept();
//                创建心脏类和消息接收类
                heartbeatDog heart = new heartbeatDog(heartbeatClient);
                workMan work = new workMan(client,topic,heart);
//                开始工作
                clientPool.submit(work);
                heartbeatClients.submit(heart);
                System.out.printf("已有客户端连接\t%s:%s\n",client.getInetAddress(),client.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    初始化服务器配置
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

//    默认服务器配置
    private static void defaultProperties(){
        port = 8888;
        clientPoolNum = 20;
        topic = "webTopic";
    }
}

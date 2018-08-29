package producer.SOCKET;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
* @Description: 创建socket服务端接收客户端请求
* @Author: Ling
* @Date: 2018/8/28
*/
public class TCP_Server {
//    服务端占用的本地端口
    private int port = 8888;
//    服务端
    private ServerSocket server;
//    创建Socket服务端
    public static void main(String[] args) throws IOException {
        TCP_Server tcp_server = new TCP_Server();
        tcp_server.server = new ServerSocket(tcp_server.port);
        System.out.printf("Socket 服务端已经启动，占用本地端口：%d",tcp_server.server.getLocalPort());
        tcp_server.start();
    }
//    启动服务端
    private void start(){
//        客户端连接池
        int clientPoolNum = 20;
//        生产者发送的topic
        String topic = "webTopic";
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
}

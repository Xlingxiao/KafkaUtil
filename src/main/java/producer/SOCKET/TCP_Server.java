package producer.SOCKET;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCP_Server {

    private int port = 8888;
    private ServerSocket server;

    public static void main(String[] args) throws IOException {
        TCP_Server tcp_server = new TCP_Server();
        tcp_server.server = new ServerSocket(tcp_server.port);
        tcp_server.start();
    }
    private void start(){
        ExecutorService clientPool = Executors.newFixedThreadPool(20);
        Socket cliet ;
        while (true){
            try {
                cliet = server.accept();
                clientPool.submit(new workMan(cliet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

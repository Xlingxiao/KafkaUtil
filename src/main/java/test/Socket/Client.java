package test.Socket;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: Socket
 * @description: 带发送心跳的客户端，一个线程发送心跳，一个线程发送消息
 * @author: Ling
 * @create: 2018/09/10 11:15
 **/
public class Client {
    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 8888;
        System.out.println("启动服务");
        Socket client = new Socket(ip,port);
        Socket heartClient = new Socket(ip,port+1);

        Msg msg = new Msg(client);
        Heart heart = new Heart(heartClient);

        new Thread(msg).start();
        new Thread(heart).start();


    }
}

class Msg implements Runnable{

    private Socket client;

    Msg(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
           Writer writer = new OutputStreamWriter(client.getOutputStream());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
            String t = client.getInetAddress()+":"+client.getPort();
            while (true){
                String msg ="{ \"" + t + "\" : \"" + format.format(new Date()) + " \" }" + "\n";
                writer.write(msg);
                writer.flush();
                System.out.println(msg);
                Thread.sleep(100*1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Heart implements Runnable{

    private Socket heart;

    Heart(Socket heartbeat) {
        this.heart = heartbeat;
    }

    public void run() {
        try {
            Writer writer = new OutputStreamWriter(heart.getOutputStream());
            while (true){
                writer.write("heartbeat\n");
                writer.flush();
                Thread.sleep(2000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

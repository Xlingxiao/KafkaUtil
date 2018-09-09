package producer.SOCKET;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @program: KafkaUtil
 * @description: 用于定时接收心跳包
 * @author: Ling
 * @create: 2018/09/09 16:40
 **/
public class heartDog implements Runnable {

    Socket heart;
    volatile boolean flag;
    public heartDog(Socket heartbeat,boolean flag) {
        this.heart = heartbeat;
        this.flag = flag;
    }

    @Override
    public void run() {
        try {
            heart.setSoTimeout(3000);
            InputStream is = heart.getInputStream();
            while (true){
                is.read();
                flag = false;
            }
        } catch (SocketTimeoutException e){
              flag = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

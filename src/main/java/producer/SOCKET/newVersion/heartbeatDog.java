package producer.SOCKET.newVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @program: KafkaUtil
 * @description: 接受客户端心跳
 * @author: Ling
 * @create: 2018/09/10 10:37
 **/

public class heartbeatDog implements Runnable{
    private boolean flag = true;
    private Socket heart;

    heartbeatDog(Socket heart) {
        this.heart = heart;
    }

    public boolean isFlag() {
        return flag;
    }

    private void setFlag() {
        this.flag = false;
    }

    @Override
    public void run() {
//        获取心跳时间为3秒
        try {
            heart.setSoTimeout(3000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        BufferedReader br = null;
        try {

            br = new BufferedReader(new InputStreamReader(heart.getInputStream()));
            String heartContent;
            while (flag){

                heartContent = br.readLine();
                System.out.println("心跳：  " + heartContent);
            }
//        捕获异常优先级顺序为 超时异常 》 Socket异常 》 I/O异常
        } catch (SocketTimeoutException e){
//            获取心跳时间出现异常将flag设置为false，结束这个客户端
            setFlag();
            System.out.println("关闭客户端");

        } catch (SocketException e){
//            获取心跳时间出现异常将flag设置为false，结束这个客户端
            System.out.println("客户端主动关闭连接");
            setFlag();

        } catch (IOException e) {

            e.printStackTrace();

//        最后一定得关闭服务端
        }finally {
            try {
                if (!heart.isClosed()){
                    br.close();
                    heart.close();
                    System.out.println("关闭心跳服务");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

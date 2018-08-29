package producer.HTTP;

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
* @Description: 提供restAPI供给客户端进行传输数据
 *  createContext中第一个参数指定url， url指定为“/”时表示接受所有请求路径
* @Author: Ling
* @Date: 2018/8/28
*/
public class httpServer {
    private static int port;
    private static int blockNumber;
    private static String topic;

//    启动服务
    private void startServer(){
        initProperties();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), blockNumber);
            List<String> contexts = initContext();
            MyHttpHandler handler = new MyHttpHandler(topic);
            for (String con : contexts){
                server.createContext(con,handler);
            }
            server.start();
            System.out.printf("HTTP服务器启动成功，请在浏览器打开%s\n",server.getAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    初始化url上下文
    private List<String> initContext() throws IOException {
//        获取上下文文件
        InputStream is = httpServer.class.getClassLoader().getResourceAsStream("http/context/contextRout");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String content ;
        List<String> contexts = new LinkedList<>();
//        将文件加入到contexts中
        while (null!=(content=br.readLine())){
            if (content.contains("#"))
                continue;
            contexts.add(content);
            System.out.println(content);
        }
        return contexts;
    }

//    初始化HTTPServer配置
    private static void initProperties(){
        Properties props = new Properties();
        InputStream is = httpServer.class.getClassLoader().getResourceAsStream("myInit.properties");
        try {
            props.load(is);
            port = Integer.parseInt(props.getProperty("httpServerPort"));
            blockNumber = Integer.parseInt(props.getProperty("httpServerBlockNumber"));
            topic = props.getProperty("topic");
        } catch (IOException e) {
            System.out.println("读取配置文件失败，将使用默认设置启动服务");
            defaultProperties();
        } catch (Exception e){
            System.out.println("读取配置文件内容有误，将使用默认设置启动服务");
            defaultProperties();
        }
    }

//    HTTPServer默认配置
    private static void defaultProperties(){
        port = 8888;
        blockNumber = 50;
        topic = "webTopic";
    }

    public static void main(String[] args) {
        httpServer thisServer = new httpServer();
        thisServer.startServer();
    }
}
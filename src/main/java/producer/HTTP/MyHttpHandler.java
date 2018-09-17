package producer.HTTP;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* @Description: 处理被上下文过滤后的请求
* @Author: Ling
* @Date: 2018/8/28
*/

public class MyHttpHandler implements HttpHandler {

    /**
     * clientConnectPool 客户端连接池
     * queueSize 处理对象集合的个数
     */
    private ExecutorService clientConnectPool;
    private String topic;
    MyHttpHandler(String topic) {
        int poolSize = 20;
        this.clientConnectPool = Executors.newFixedThreadPool(poolSize);
        this.topic = topic;
    }

    /**
     * 接收到客户端请求后会调用到这个方法
     * 使用线程池对客户端请求进行处理
     * @param httpExchange httpServer包中使用这个对象和客户端交互
     */
    public void handle(HttpExchange httpExchange){
        workMan work = new workMan(httpExchange,topic);
        clientConnectPool.submit(work);
    }
}

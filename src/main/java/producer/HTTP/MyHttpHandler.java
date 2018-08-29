package producer.HTTP;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
     * workMEN 主要工作对象池
     * workMan 主要工作对象
     */
    ExecutorService clientConnectPool;
    private int queueSize = 20;
    private BlockingQueue workMEN = new ArrayBlockingQueue(queueSize);
    private workMan work ;

    MyHttpHandler() {
        this.clientConnectPool = Executors.newFixedThreadPool(20);
    }

    /**
     * 接收到客户端请求后会调用到这个方法
     * 使用线程池对客户端请求进行处理
     * @param httpExchange httpServer包中使用这个对象和客户端交互
     */
    public void handle(HttpExchange httpExchange){
        work = new workMan(httpExchange);
        clientConnectPool.submit(work);
    }
}

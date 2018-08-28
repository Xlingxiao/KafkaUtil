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
    ExecutorService clientConnectPool = null;
    int queueSize = 2;
    BlockingQueue workMEN = new ArrayBlockingQueue(queueSize);
    workMan work ;

    public MyHttpHandler() {
        this.clientConnectPool = Executors.newFixedThreadPool(2);
    }

    /**
     * 接收到客户端请求后会调用到这个方法
     * 使用线程池对客户端请求进行处理
     * @param httpExchange
     * @throws IOException
     */
    public void handle(HttpExchange httpExchange) throws IOException {
        if(workMEN.size()<queueSize){
            work = new workMan(httpExchange);
        }else {
            work = (workMan) workMEN.poll();
            work.setHttpExchange(httpExchange);
        }
        clientConnectPool.submit(work);
        workMEN.add(work);
    }
}

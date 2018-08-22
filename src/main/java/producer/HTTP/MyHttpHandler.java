package producer.HTTP;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @创建人
 * @创建时间
 * @描述
 */
public class MyHttpHandler implements HttpHandler {

    ExecutorService clientConnectPool = null;

    int queueSize = 2;

    BlockingQueue workMEN = new ArrayBlockingQueue(queueSize);

    workMan work ;

    public MyHttpHandler() {
        this.clientConnectPool = Executors.newFixedThreadPool(2);
    }

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

package producer.HTTP;

import com.sun.net.httpserver.HttpExchange;
import producer.demo.myProducer;

import java.io.*;
import java.util.Objects;

/** 
 * @Description:  接收客户端请求使用request进行获取请求内容
 *                 使用response进行响应
 * @Author: Ling
 * @Date: 2018/8/28
 */

public class workMan implements Runnable {

    private HttpExchange httpExchange ;
//    请求方式
    private String requestMethod ;
//    请求地址
    private String requestUrl ;
//    响应状态码
    private int statusCode;
//    响应地址
    private String responseUrl;
//    请求内容
    private StringBuilder stringBuilder;
//    kafka producer
    private myProducer producer ;
//    kafka Topic
    private String topic;
    workMan(HttpExchange httpExchange,String topic) {
        this.httpExchange = httpExchange;
        stringBuilder = new StringBuilder();
        this.topic = topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    private void setResponseUrl() {
        if (statusCode!=200){
            responseUrl = "http/Exception/404.html";
        }else
            responseUrl = "http/success"+requestUrl;
    }

    /**
     * 得到请求内容
     * 响应客户端
     * 使用producer发送消息
     */
    public void run() {
        try {
            httpRequest(httpExchange);
//            setResponseUrl();
//            httpResponse(httpExchange);
            sHttpResponse(httpExchange);
            if(stringBuilder.length()>0){
                sendToKafka();
            }
            httpExchange.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取客户端请求过来的值
     * @param exchange 交换对象 httpServer包内使用这个对象进行和客户端的交互操作
     * @throws IOException 获取请求内容时可能出现I/O异常
     */
    private void httpRequest(HttpExchange exchange) throws IOException {
//        获取请求方式、请求URL、设置响应状态码
        requestMethod = exchange.getRequestMethod();
        requestUrl = exchange.getRequestURI().getPath();
        System.out.printf("请求方式: %s \t请求路径: %s\n", requestMethod, requestUrl);
//        请求方式为post开始处理
        if (requestMethod.compareToIgnoreCase("post")==0){
            statusCode = 200;
//        将请求内容添加进StringBuilder中
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String msg;
            while (null!=(msg=br.readLine())){
                msg = msg.replaceAll("\\s*","");
                stringBuilder.append(msg);
            }
            System.out.println(stringBuilder.toString());
            br.close();
        }else if(requestMethod.compareToIgnoreCase("get")==0){
            statusCode = 404;
        }
    }

    /**
     * 使用文件对其响应
     * @param exchange 与客户端主要的数据交换对象
     */
    private void httpResponse(HttpExchange exchange) {
//        响应文件
        File file = null;
//        响应文件的字节内容
        byte[] fileConnect = null;
//        响应文件的路径
        String url;
        try {
            url = Objects.requireNonNull(MyHttpHandler.class.getClassLoader().getResource(responseUrl)).getPath();
            file = new File(url);
            fileConnect = new byte[(int) file.length()];
        }catch (Exception e){
            statusCode =404;
            httpResponse(exchange);
        }
        FileInputStream fi;
        try {
            assert file != null;
            fi = new FileInputStream(file);
            assert fileConnect != null;
            fi.read(fileConnect);
            fi.close();
            exchange.sendResponseHeaders(statusCode, fileConnect.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileConnect);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收到客户端的请求后对客户端及进行响应
     * @param exchange 交换对象 httpServer包内使用这个对象进行和客户端的交互操作
     */
    private void sHttpResponse(HttpExchange exchange){
        String responseContent;
        if (statusCode==200){
            responseContent = stringBuilder.toString();
        }
        else{
            responseContent = "<h1>404 Not Found</h1>No context found for request";
        }
        try {
            exchange.sendResponseHeaders(statusCode, responseContent.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseContent.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToKafka(){
        if (producer==null)
            producer = new myProducer();
        producer.sendMsg(topic,stringBuilder);
    }
}

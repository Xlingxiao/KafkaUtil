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
    private String topic = "webTopic" ;
    workMan(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        stringBuilder = new StringBuilder();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl() {
        if (getStatusCode()!=200){
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
            setResponseUrl();
            httpResponse(httpExchange);
            if(stringBuilder!=null){
                sendToKafka();
            }
//            使用完StringBuilder之后进行清空否则会导致StringBuilder越来越大
            stringBuilder.delete(0,stringBuilder.length());
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
//        获取请求方式、请求URL、设置响应URL
        requestMethod = exchange.getRequestMethod();
        requestUrl = exchange.getRequestURI().getPath();
        System.out.printf("请求方式: %s \t请求路径: %s\n", requestMethod, requestUrl);
        if (requestMethod.compareToIgnoreCase("post")==0){
            setStatusCode(200);
//        将请求内容添加进StringBuilder中
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String body;
            while (null != (body = br.readLine())) {
                stringBuilder.append(body);
            }
            System.out.println(stringBuilder.toString());
            br.close();
        }else if(requestMethod.compareToIgnoreCase("get")==0){
            setStatusCode(404);
        }
    }

    /**
     * 接收到客户端的请求后对客户端及进行响应
     * @param exchange 交换对象 httpServer包内使用这个对象进行和客户端的交互操作
     */
    private void httpResponse(HttpExchange exchange) {
//        响应文件
        File file = null;
//        响应文件的字节数组长度
        Long fLength = null;
//        响应文件的字节内容
        byte[] fileConnect = null;
//        响应文件的路径
        String url;
        try {
            url = Objects.requireNonNull(MyHttpHandler.class.getClassLoader().getResource(getResponseUrl())).getPath();
            file = new File(url);
            fLength = file.length();
            fileConnect = new byte[fLength.intValue()];
        }catch (Exception e){
            setStatusCode(404);
            httpResponse(exchange);
        }
        FileInputStream fi;
        try {
            assert file != null;
            fi = new FileInputStream(file);
            assert fileConnect != null;
            fi.read(fileConnect);
            fi.close();
            exchange.sendResponseHeaders(200, fLength);
            OutputStream os = exchange.getResponseBody();
            os.write(fileConnect);
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

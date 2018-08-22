package producer.HTTP;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;

/**
 * @创建人
 * @创建时间
 * @描述
 */
public class workMan implements Runnable {

    private HttpExchange httpExchange ;
//    请求方式
    private String requestMethod ;
//    请求地址
    private String requestUrl ;
//    响应地址
    private String responseUrl;
//    请求内容
    private StringBuilder sb = new StringBuilder();
//    kafka producer
    private myProducer producer ;
//    kafka Topic
    private String topic = "test" ;
    public workMan(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public void setHttpExchange(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public void run() {
        try {
            httpRequest(httpExchange);
            httpResponse(httpExchange);
            sendToKafka();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void httpRequest(HttpExchange exchange) throws IOException {
        requestMethod = exchange.getRequestMethod();
        requestUrl = exchange.getRequestURI().getPath();
        responseUrl = "http/success"+requestUrl;
        System.out.printf("请求方式: %s \t请求路径: %s\n", requestMethod, requestUrl);
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String body;
        while (null != (body = br.readLine())) {
            System.out.println(body);
            sb.append(body);
        }
    }

    private void httpResponse(HttpExchange exchange) {
        File file = null;
        Long flenth = null;
        byte[] fileConnect = null;
        String url;
        int recode ;
        try {
            url = MyHttpHandler.class.getClassLoader().getResource(responseUrl).getPath();
            file = new File(url);
            flenth = file.length();
            fileConnect = new byte[flenth.intValue()];
        }catch (Exception e){
            System.out.println("请求地址在上下文中有但是没有响应的html");
            responseUrl = "http/Exception/404.html";
            httpResponse(exchange);
        }
        FileInputStream fi = null;
        try {
//            String requestUrl = exchange.getRequestURI().getPath();
//            System.out.println(requestUrl);
            fi = new FileInputStream(file);
            fi.read(fileConnect);
            fi.close();
            exchange.sendResponseHeaders(200, flenth);
            OutputStream os = exchange.getResponseBody();
            os.write(fileConnect);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToKafka(){
        if (producer==null)
            producer = new myProducer();
        producer.sendMsg(topic,sb);
    }
}

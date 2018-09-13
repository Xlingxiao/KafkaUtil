package producer.demo;

import java.io.*;
import java.util.Properties;

/**
 * @创建人
 * @创建时间
 * @描述
 */
public class getProperties {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        InputStream is = getProperties.class.getClassLoader().getResourceAsStream("kafka/producer.properties");
        props.load(is);
        is.close();
        System.out.println(props.toString());
    }
}

package common;

import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;


/**
 * @program: KafkaUtil
 * @description:
 * @author: Ling
 * @create: 2018/09/13 18:28
 **/
public class MySerializer implements Serializer<Object>{
    @Override
    public void configure(Map configs, boolean isKey) {

    }
    @Override
    public byte[] serialize(String topic, Object data) {
        return BeanUtils.bean2Byte(data);
    }
    /*
     * producer调用close()方法是调用
     */
    @Override
    public void close() {
        System.out.println("Encoding Kafka is close");
    }

}

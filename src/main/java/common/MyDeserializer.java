package common;



import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

/**
 * @program: KafkaUtil
 * @description: 自定义序列化Map对象
 * @author: Ling
 * @create: 2018/09/13 17:55
 **/
public class MyDeserializer implements Deserializer<Object> {


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        return BeanUtils.byte2Obj(data);
    }

    @Override
    public void close() {

    }
}

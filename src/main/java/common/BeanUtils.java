package common;

import java.io.*;
import java.util.Map;

/**
 * @program: KafkaUtil
 * @description: 将对象/二进制流 转换
 * @author: Ling
 * @create: 2018/09/13 18:37
 **/
public class BeanUtils {

    private BeanUtils() {}

    /**
     * 对象序列化为byte数组
     */
    public static byte[] bean2Byte(Object obj) {
        byte[] bb = null;
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArray)){
            outputStream.writeObject(obj);
            outputStream.flush();
            bb = byteArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bb;
    }

    /**
     * 字节数组转为Object对象
     */
    public static Object byte2Obj(byte[] bytes) {
        Object readObject = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             ObjectInputStream inputStream = new ObjectInputStream(in)){
            readObject = inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readObject;
    }

}

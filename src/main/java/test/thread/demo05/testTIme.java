package test.thread.demo05;/**
 * @创建人
 * @创建时间
 * @描述
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @program: KafkaUtil
 * @description: testTime
 * @author: Ling
 * @create: 2018/08/31 16:09
 **/
public class testTIme {
    public static void main(String[] args) throws ParseException {
        Calendar test = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String qq = format1.format(test.getTime());
        Date newDate = format1.parse(qq);
        System.out.println(format.format(test.getTime()));
        System.out.println(format.format(newDate));
        System.out.println(test.getTimeInMillis());
        System.out.println(newDate.getTime());

    }
}

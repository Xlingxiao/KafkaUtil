package thread.demo05;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @program: FTPtest
 * @description: 测试java的定时任务
 * @author: Ling
 * @create: 2018/08/30 16:27
 **/
public class timedTask {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
        for (int i = 0; i < 1; ++i) {
            Test test = new Test();
            executor.scheduleWithFixedDelay(test,1,1,TimeUnit.SECONDS);
        }
//        executor.shutdown();
    }
}

class Test implements Runnable{

    public void run() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
        Calendar calendar = Calendar.getInstance();
        String time = simpleDateFormat.format(calendar.getTime());
        System.out.println(Thread.currentThread().getName() +" " + time);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

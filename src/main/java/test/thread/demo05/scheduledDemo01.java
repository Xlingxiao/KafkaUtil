package test.thread.demo05;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: KafkaUtil
 * @description: Scheduled基本使用方法1
 * @author: Ling
 * @create: 2018/08/30 17:34
 **/
public class scheduledDemo01 {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
        for (int i = 0; i < 1; i++) {
            executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName()+" run");
                }
            },1,1, TimeUnit.SECONDS);
        }
    }
}

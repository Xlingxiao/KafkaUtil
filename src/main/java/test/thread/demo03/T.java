package test.thread.demo03;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @创建人
 * @创建时间
 * @描述 多线程共享同一份资源，
 * 在实现了Runable接口的类中定义一个需要共享的变量
 * 在主线程中创建这个类的对象
 * 使用这个对象来创建线程对象
 * 启动线程就会发现线程共享了内部的变量
 */
public class T {
    public static void main(String[] args) {
        List<Thread> threadList = new ArrayList<Thread>();
        myThread thread =  new myThread();
        int countThread = 2;
        for(int i =0;i<countThread;i++){
            Thread t = new Thread(thread);
            threadList.add(t);
        }
        for(int i = 0 ;i<countThread;i++){
            threadList.get(i).start();
        }
    }
}

class myThread implements Runnable {
    Lock lock = new ReentrantLock();
    private int count = 100;
    public void run() {
        while (count>0){
            getTicket();
        }
    }

    void getTicket(){
        lock.lock();
        try{
            if(count>0){
                count--;
                System.out.println(Thread.currentThread().getName()+"抢到一张票,现在还剩："+count+" 张票");
            }
        }catch (Exception e){
            System.out.println("抢票内部出现异常");
            lock.unlock();
        }finally {
            lock.unlock();
        }
    }
}

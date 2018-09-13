package test.thread.demo04;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @创建人
 * @创建时间
 * @描述 模拟生产者消费者问题，在消费者处使用线程锁避免取到空对象
 * 使用sleep()函数扩大出错范围，真实使用不需要sleep()
 */
public class ThreadAndQueue {

    public static volatile boolean flag = true;

    public static void main(String[] args) {
        BlockingQueue queue = new ArrayBlockingQueue(10);
        Producer producer = new Producer(queue,flag);
        Consumer consumer = new Consumer(queue,flag);
        for (int i =0;i<1;i++){
            new Thread(producer,"生产者-"+i).start();

//            new Thread(consumer,"消费者-"+i).start();
        }
        for (int i =0;i<1;i++){
//            new Thread(producer,"生产者-"+i).start();
            new Thread(consumer,"消费者-"+i).start();
        }

    }
}

class Producer implements Runnable {
    Lock lock = new ReentrantLock();
    BlockingQueue queue ;
    public static volatile boolean flag = true;

    public Producer(BlockingQueue queue,boolean flag) {
        this.queue = queue;
        this.flag = flag;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                if (this.queue.offer(i,10, TimeUnit.SECONDS))
                    System.out.println(Thread.currentThread().getName()+"生产了："+i);
                else
//                    10秒内没有得到插入就结束循环
                    break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        flag = false;
    }
}

class Consumer implements Runnable {
    public static volatile boolean flag = true;
    BlockingQueue queue;
    Lock lock = new ReentrantLock();
    public Consumer(BlockingQueue queue,boolean flag) {
        this.queue = queue;
        this.flag = flag;
    }

    public void run() {
        while (flag){
            try {
                Thread.sleep(100);
                lock.lock();
                if (queue.isEmpty()){
                    lock.unlock();
                    continue;
                }
                System.out.println(Thread.currentThread().getName()+"消费了："+ this.queue.poll());
                lock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

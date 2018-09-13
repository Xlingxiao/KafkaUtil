package test.thread.demo02;

/**
 * @创建人
 * @创建时间
 * @描述 实现runable接口实现多线程
 */
public class T {
    public static void main(String[] args) {
        for(int i = 0;i<10;i++){
            testThread test = new testThread();
            Thread t = new Thread(test);
            t.start();
        }
    }
}

class testThread implements Runnable {

    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}
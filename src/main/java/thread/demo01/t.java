package thread.demo01;

/**
 *@功能
 *@描述 继承Thread类实现多线程
 *@创建人  Lingxiao
 *@创建时间
 */
public class t {
    public static void main(String[] args) {
        for(int i = 0;i<10;i++){
            new testThread().start();
        }

    }
}

class testThread extends Thread {

    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
}
package Consumer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class copy_file {
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        //思路：先把图片读入到内存--》写入某个文件
        //因为是二进制文件，因此只能用字节流完成
        //输入流
        FileInputStream fis=null;
        //输出流
        FileOutputStream fos=null;
        //用路径 “d:/a.jpg” 代替f   File f=new File( “d:/a.jpg”)
        try {
            fis=new FileInputStream("e:/a.png");
            fos=new FileOutputStream("e:/b.png");

            byte bytes[]=new byte[1024];

            int n=0;  //记录实际读取到的字节数
            while((n=fis.read(bytes))!=-1)
            {
                //输出到指定文件
                fos.write(bytes);

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally{
            //关闭流
            try {
                fis.close();
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

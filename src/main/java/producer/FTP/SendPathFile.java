package producer.FTP;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.kafka.clients.producer.Producer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static producer.FTP.SendOneFile.*;
/**
 *@功能 从ftp服务器下载指定目录的所有文件使用producer传输
 *@描述   AllFileText() 深度优先遍历指定目录
 *        获取到文件时调用ProcessContent()方法解析
 *        使用producer 将本次解析内容进行发送
 *        整个过程中Ftpclien和Producer只创建一次
 *@创建人  Lingxiao
 *@创建时间  2018/8/11
 */

public class SendPathFile {

    private static void AllFileText(FTPClient ftpClient, String path,Producer<String,String> producer, String topic){
        try {
//            FTP协议默认只支持iso-8859-1的编码格式，
//            这里我们转换中文文件名为字节形式
//            将字节形式转为iso-8859-1的编码
            path = new String(path.getBytes("UTF-8"),"iso-8859-1");
//            判断改变工作路径是否成功
            boolean ff = ftpClient.changeWorkingDirectory(path);
            if(ff){
                FTPFile[] fs = ftpClient.listFiles();
//                    只获取服务器上的前十个文件
                for(int i =0;i<5;i++){
//                    判断文件路径下没有那么多文件的话就退出
                    if (fs.length<=i)
                        break;
                    FTPFile file = fs[i];
                    System.out.println(path+"/"+file.getName());
                    if (file.isDirectory()){
                        AllFileText(ftpClient,file.getName(),producer, topic);
                    }
                    else if(file.isFile()){
//                        将文件名转为iso-8859-1
                        String name = new String(file.getName().getBytes("UTF-8"),"iso-8859-1");
                        InputStream is = ftpClient.retrieveFileStream(name);
                        BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
//                    解析ftp文件
                        StringBuilder sb = ProcessContent(br);
                    /*官方要求在调用retrieveFileStream()方法下载文件时必须有执行
                    completePendingCommand()，等FTP Server返回226 Transfer complete
                    但是FTP Server只有在接受到InputStream 执行close方法时，才会返回。
                    所以一定先要执行close方法。不然在第一次下载一个文件成功之后，
                    之后再次获取inputStream 就会返回null。*/
                        br.close();
                        is.close();
                        ftpClient.completePendingCommand();
//                      这里调用了上面的SendOneFile里面的Producer方法
                        StartProducer(producer,topic,sb);
                    }
                    else
                        break;
                }
//                遍历一个目录之后退出
                ftpClient.changeToParentDirectory();
            }
            else{
                System.out.println("更改FTP工作路径失败！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpLoadFromString(){
        String topic = "test";
        String remotePath = "./code/jupyter/tmp/";
        FTPClient ftpClient = getFtpClient();
        if (ftpClient!=null){
            Producer<String,String> producer = getProducer();
            AllFileText(ftpClient,remotePath,producer,topic);
            endProducer(producer,topic);
        }
        else{
            System.out.println("获取ftp对象失败");
        }
        System.out.println("程序执行完毕！");
    }
}

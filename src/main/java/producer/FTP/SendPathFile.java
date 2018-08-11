//package producer.FTP;
//
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPReply;
//import org.junit.jupiter.api.Test;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//
//
//import static producer.FTP.SendOneFile.StartProducer;
//
//public class SendPathFile {
//    public static boolean downFile(String url, int port,String username, String password, String remotePath) {
//        boolean success = false;
//        FTPClient ftp = new FTPClient();
//        try {
//            int reply;
//            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
//            ftp.connect(url, port);
//            ftp.login(username, password);//登录
////            获取链接状态码
//            reply = ftp.getReplyCode();
////            验证链接状态码
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                ftp.disconnect();
//                return success;
//            }
////            nat网络环境下需要设置为被动模式
//            ftp.enterLocalPassiveMode();
////            开始遍历的主方法
//            AllFileText(ftp,remotePath);
////            退出ftp登录
//            ftp.logout();
//            success = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (ftp.isConnected()) {
//                try {
//                    ftp.disconnect();
//                } catch (IOException ioe) {
//                }
//            }
//        }
//        return success;
//    }
//    private static void AllFileText(FTPClient ftpClient, String path){
//        try {
////            判断改变工作路径是否成功
//            boolean ff = ftpClient.changeWorkingDirectory(path);
//            if(ff){
//                //            System.out.println(ff);
//                FTPFile[] fs = ftpClient.listFiles();
////                    只获取服务器上的前十个文件
//                for(int i =0;i<10;i++){
//                    FTPFile file = fs[i];
//                    System.out.println(path+"/"+file.getName());
//                    if (file.isDirectory()){
//                        AllFileText(ftpClient,file.getName());
//                    }
//                    InputStream is = ftpClient.retrieveFileStream(file.getName());
//                    BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
//                    String msg = "";
//                    StringBuilder sb = new StringBuilder();
//                    while ((msg=br.readLine())!= null) {
//                        sb.append(msg);
//                        sb.append("\r\n");
////                    System.out.println(msg);
//                    }
//                    /*官方要求在调用retrieveFileStream()方法下载文件时必须有执行
//                    completePendingCommand()，等FTP Server返回226 Transfer complete
//                    但是FTP Server只有在接受到InputStream 执行close方法时，才会返回。
//                    所以一定先要执行close方法。不然在第一次下载一个文件成功之后，
//                    之后再次获取inputStream 就会返回null。*/
//                    is.close();
//                    ftpClient.completePendingCommand();
////                这里调用了上面的SendOneFile里面的Producer方法
//                    StartProducer(sb);
//                }
//            }
//            else{
//                System.out.println("更改FTP工作路径失败！");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testUpLoadFromString(){
//        //ftp服务器地址
//        String hostname = "xx.xx.xx.xx";
//        //ftp服务器端口号默认为21
//        Integer port = 21 ;
//        //ftp登录账号
//        String username = "hadoop";
//        //ftp登录密码
//        String password = "123456";
////            本地路径
//        String localPath = "E:/tmp";
//        boolean flag = downFile(hostname, port, username, password, "./code/jupyter/tmp/haerbing/");
//        System.out.println(flag);
//    }
//}

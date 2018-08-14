# KafkaUtil
kafka 的java基本使用
1.	单程从ftp服务器获取文件目录
2.	多线程从FTP服务器获取文件
3.	对下载的文件进行处理，只有框架（内部没有方法）
4.	处理过的数据传给kafkaProducer，进行发送
主要结构：
1．	Controller类负责控制这个程序的运行，内部有两个内部类，getFilePath和consumerFilePath，使用生产者消费者模式运行。
2．	getFilePath：生产url，目前只支持一个producer
3．	consumerFilePath() 使用url下载文件并交给processContent处理，处理后的内容交给producer发送
4．	processContent 目前里面只有一个空的方法，以后用做处理文件内容用。
5．	FTPUtil：提供FTPClient的相关工具，
getFtpClient()	获取FTP对象。
AllFilePath(FTPClient ftpClient,BlockingQueue queue, String path)	遍历目录添加到队列之中
StringBuilder getDownlod(FTPClient ftpClient,String path)	得到队列中一个文件的内容
endFtp(FTPClient ftpClient)	关闭FTP对象
5.	myProducer:
因为kafka producer后面的版本都是线程安全的了，所以采用单例模式创建producer
使用单身模式创建一个KafkaProducer(),在需要使用到它发送消息的时候调用
sendMsg()方法就可以了。

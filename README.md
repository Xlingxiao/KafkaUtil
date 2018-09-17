#new
    将文件路径存放的对列长度设置改为读取配置文件
    将任务定时执行时间改为读取配置文件

# KafkaUtil
kafka 的java基本使用

#FTP方向
1.使用生产者消费者方式生产者负责从ftp服务器获取文件名，消费者负责根据文件名直接下载文件

2.消费者将下载的文件进行解析后交给kafkaProducer，进行发送

3.定时在一分钟扫描一次指定文件夹发先新的文件进行下载并传输

4.生产文件路径的线程在8s钟内不能生产指定数量的文件路径就会被关闭

5.程序将下载的FTP文件使用resources/FTP/filefield文件进行解析为json数据
逐条发送到kafka服务器，

#HTTP方向
1.使用jdk自带的httpServer包构建

2.通过读取配置文件配置http服务部署的端口以及支持阻塞的客户端数量

3.读取resources/http/contextRout文件指定httpServer接收的请求路径，
不再此路径内的请求地址返回404错误

4.获取客户端请求后获取请求方式 请求内容 请求url 如果请求方式不是post方式直接返回404，
如果请求内容不为空，将请求内容返回给client并将请求内容发送给kafka，
如果请求内容为空不会将请求内容发送给kafka，
#Socket方向
1.读取配置文件配置服务占用的本地端口以及设置客户端连接池的大小

2.使用长连接的方式连接client，client必须定时（3秒）向指定的heart端口发送
心跳包，服务端没有检测到心跳包就会主动将连接断开，目前使用的心跳机制只测试
一次客户端连接，应该改为尝试3-5次都没有收到心跳再进行关闭客户端操作，

3.对客户端发送过来的消息进行接收发送给kafkaProducer

    
#目前存在的问题
目前里面都没有设置对客户端发送的内容是不是json数据进行检测，感觉不是很安全

#各个函数功能：
   在代码内部几乎每句代码是做什么的都有注释，这里就不细说了

#consumer
consumer/demo04中consumer会自动获取kafka topic中的分区数量以此
获取创建多少个线程处理消息，保证每个线程获取一个分区中的消息。

old(现在传输的对象是String类型的，我们后面应该改为传输指定类型的数据，这样就可以
    省略多次进行类型转换数据解析的操作)
new：在各个客户端发送数据给我之后我不需要进行任何操作，
    直接将数据发送给kafka解析为json,然后存进ES数据库，
    中途也没有发生多次的数据类型转换。
    
kafka支持直接传送对象给kafka，consumer接收的时候收到的也是对象
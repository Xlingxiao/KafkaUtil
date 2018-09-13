package common;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @program: KafkaUtil
 * @description: 测试Elasticsearch
 * @author: Ling
 * @create: 2018/09/12 18:13
 **/
public class ESUtil {

    private TransportClient client;
    private String elasticIp;
    private int elasticPort;


    public ESUtil(String elasticIp) throws UnknownHostException {
        this.elasticIp = elasticIp.split(":")[0];
        this.elasticPort = Integer.parseInt(elasticIp.split(":")[1]);
        this.init();
    }

    //    初始化ES操作对象
    private void init() throws UnknownHostException {
        Settings esSettings = Settings.builder().put("cluster.name", "elasticsearch").build();
        client = new PreBuiltTransportClient(esSettings);
        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(elasticIp), elasticPort));
        System.out.println("连接建立成功");
    }

    /**
     * Get index 获取文档相当于读取数据库的一行数据
     */
    public void getIndex(int id){
        GetResponse getresponse = client.prepareGet("zxtest", "person", id+"").execute().actionGet();
        System.out.println(getresponse.getSourceAsString());
    }

    /**
     * 获取服务器上所有Index
     */
    public void getAllIndex(){
        ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
        //获取所有索引
        String[] indexes = response.getState().getMetaData().getConcreteAllIndices();
        for (String index : indexes) {
            System.out.println(index);
        }
    }


    /**
     * 向指定位置插入一条数据
     * @Throws ElasticsearchException
     */
    public void insertOneData(String index,String type,Map<String,?> msg) throws ElasticsearchException {
        IndexResponse indexResponse;
        indexResponse = client.prepareIndex(index, type).setSource(msg).execute().actionGet();
        System.out.println("responseIsCreated: "+indexResponse);
        System.out.println("Insert it's ok ！");
    }

    /*
     *Delete index 相当于删除一行数据
     */
    public void delete(int id){
        DeleteResponse deleteresponse = client.prepareDelete("zxtest", "person",id+"").execute().actionGet();
        System.out.println(deleteresponse.getVersion());
    }

    public void close(){
        //on shutdown 断开集群
        client.close();
    }
}

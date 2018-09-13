//package test;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//
//import org.elasticsearch.ElasticsearchException;
//import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
//import org.elasticsearch.action.delete.DeleteResponse;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.TransportAddress;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentFactory;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.search.SimpleQueryStringQueryParser;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.sort.SortOrder;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//
///**
// * @program: KafkaUtil
// * @description: 测试Elasticsearch
// * @author: Ling
// * @create: 2018/09/12 18:13
// **/
//public class ElasticsearchUtil {
//
//    private static TransportClient client;
//    private static String elasticIp = "172.16.2.103";
//    private static int elasticPort = 9300;
//
//    public static void init() throws UnknownHostException, InterruptedException, ExecutionException {
//        Settings esSettings = Settings.builder().put("cluster.name", "elasticsearch").build();
//        client = new PreBuiltTransportClient(esSettings);
//        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
//        client.addTransportAddress(new TransportAddress(InetAddress.getByName(elasticIp), elasticPort));
//        System.out.println("连接建立成功");
//    }
//    /*
//     * Get index 获取文档相当于读取数据库的一行数据
//     */
//    public static void getIndex(int id){
//        GetResponse getresponse = client.prepareGet("zxtest", "person", id+"").execute().actionGet();
//        System.out.println(getresponse.getSourceAsString());
//    }
//
//    /**
//     * 获取服务器上所有Index
//     */
//    public static void getAllIndex(){
//        ClusterStateResponse response = client.admin().cluster().prepareState().execute().actionGet();
//        //获取所有索引
//        String[] indexes = response.getState().getMetaData().getConcreteAllIndices();
//        for (String index : indexes) {
//            System.out.println( index + " delete" );//
//        }
//    }
//
//
//    /**
//     * 向指定位置插入一条数据
//     * @throws ElasticsearchException
//     */
//    public static void createIndex(int id) throws ElasticsearchException {
//        Map<String,String> tmp = new HashMap<>();
//        tmp.put("8080", "tomcat");
//        tmp.put("21","FTP");
//        IndexResponse indexResponse;
//        indexResponse = client.prepareIndex("zxtest", "person",id+"").setSource(tmp).execute().actionGet();
//        System.out.println("responseIsCreated: "+indexResponse);
//        System.out.println("it is ok ！");
//    }
//
//    /*
//     *Delete index 相当于删除一行数据
//     */
//    public static void delete(int id){
//        DeleteResponse deleteresponse = client.prepareDelete("zxtest", "person",id+"").execute().actionGet();
//        System.out.println(deleteresponse.getVersion());
//    }
//
//    public void close(){
//        //on shutdown 断开集群
//        client.close();
//    }
//
//    public static void main(String[] args) throws InterruptedException, ExecutionException, UnknownHostException {
//        init();
//        int id = 4;
////        createIndex(id);
////        getIndex(id);
////        delete(id);
////        getAllIndex();
//
//    }
//}

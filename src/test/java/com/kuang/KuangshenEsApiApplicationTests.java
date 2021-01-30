package com.kuang;

import com.alibaba.fastjson.JSON;
import com.kuang.entity.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class KuangshenEsApiApplicationTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
   public void testCreateIndex() throws IOException {
        //创建索引请求
        CreateIndexRequest request=new CreateIndexRequest("jd_goods");
        //客户端执行请求
        CreateIndexResponse response=restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
        public void testExistIndex() throws IOException {
        //获取索引
        GetIndexRequest request=new GetIndexRequest("chuxia");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    @Test
    public void testDeleteExistIndex() throws IOException {
        //删除索引
        DeleteIndexRequest request=new DeleteIndexRequest("chuxia");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(delete);
    }
    @Test
    //添加文档
    public void testAddDocument() throws IOException{
        //创建对象
        User user=new User("heisi",18);
        //创建请求
        IndexRequest request=new IndexRequest("chuxia");
        //添加规则
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");
        //将我们的请求放入请求
        IndexRequest source = request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求，获取响应的结果
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(index.toString());//
        System.out.println(index.status());//对应我们命令的返回状态  CREATED
    }
    @Test
    //获取文档，判断文档是否存在
    public void testIsExists() throws IOException{
        GetRequest request=new GetRequest("chuxia","1");
        boolean exists = restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    @Test
    //获取文档信息
    public void testGetDocument() throws IOException{
        GetRequest request=new GetRequest("chuxia","1");
        GetResponse documentFields = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println(documentFields.getSourceAsString());//打印文档的内容
        System.out.println(documentFields);//返回全部内容
    }
    @Test
    //修改文档信息
    public void testUpdateRequest() throws IOException {
        UpdateRequest request=new UpdateRequest("chuxia","1");
        User user=new User("黑丝",20);
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse update = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(update.status());
    }
    @Test
    //删除文档信息
    public void testDeleteRequest() throws IOException {
        DeleteRequest request=new DeleteRequest("chuxia","1");
        request.timeout("1s");//这里的意思是超过一秒还没执行成功就不执行了
        DeleteResponse delete = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }
    @Test
    //批量插入数据
    public void testBulkRequest() throws IOException {
        BulkRequest request=new BulkRequest();
        request.timeout("5s");
       // ArrayList<User> list=new ArrayList<>();
        List<User>list=new ArrayList();
        list.add(new User("黑丝",18));
        list.add(new User("白丝",22));
        list.add(new User("吊带袜",20));
        list.add(new User("连裤袜",24));

        for (int i = 0; i < list.size(); i++) {
            //批量更新和删除就在这里改动就可以了
            request.add(
                  new IndexRequest("chuxia")
                  .id(""+(i+1))
                  .source(JSON.toJSONString(list.get(i)),XContentType.JSON));

        }
        BulkResponse bulk = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());
    }
    @Test
    //自定义查询
    public void testSeacher() throws IOException {
        //指定查询哪个索引
        SearchRequest request=new SearchRequest("chuxia");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        //精确匹配查询
        TermQueryBuilder builder= QueryBuilders.termQuery("name","heisi");
        searchSourceBuilder.query(builder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        request.source(searchSourceBuilder);

        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(search.getHits()));
        System.out.println("================================");

        for (SearchHit documentFields : search.getHits().getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }
    }
}

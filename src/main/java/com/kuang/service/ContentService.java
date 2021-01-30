package com.kuang.service;


import com.alibaba.fastjson.JSON;
import com.kuang.entity.Content;
import com.kuang.utils.HtmlParseUtil;
import com.sun.org.apache.xpath.internal.compiler.Keywords;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public Boolean parseConten(String Keywords) throws Exception {
        List<Content>contentList=new HtmlParseUtil().parseJD(Keywords);
        //把查询的数据放到Es中
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.timeout("2m");

        for (int i = 0; i < contentList.size(); i++) {
            System.out.println(JSON.toJSONString(contentList.get(i)));
            bulkRequest.add(
                    new IndexRequest("jd_good")
                    .source(JSON.toJSONString(contentList.get(i)), XContentType.JSON)
            );
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }
    //获取这些数据实现搜索功能
    public List<Map<String,Object>>searchPage(String keyword,int pageNo,int pageSize) throws IOException {
        if(pageNo<=1){
            pageNo=1;
        }

        //条件搜索
        SearchRequest request=new SearchRequest("jd_good");
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();

        //分页查询
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //精确匹配
        TermQueryBuilder termQueryBuilder= QueryBuilders.termQuery("title",keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        request.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //解析结果
        List<Map<String,Object>>list=new ArrayList<>();
        for (SearchHit documentFieIds : searchResponse.getHits().getHits()) {
            list.add(documentFieIds.getSourceAsMap());
        }
        return list;
    }
    //实现搜索高亮显示
    public List<Map<String,Object>>searchPageHightBuilder(String keyword,int pageNo,int pageSize) throws IOException {
        if(pageNo<=1){
            pageNo=1;
        }

        //条件搜索
        SearchRequest request=new SearchRequest("jd_good");
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();

        //分页查询
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //精确匹配
        TermQueryBuilder termQueryBuilder= QueryBuilders.termQuery("title",keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮显示
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        request.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //解析结果
        List<Map<String,Object>>list=new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields =hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();//原来的结果
            //解析高亮的字段,将高亮的字段替换原来的字体
            if(title!=null){
                Text[] fragments = title.fragments();//
                String n_title="";
                for (Text text : fragments) {
                    n_title+=text;
                }
                sourceAsMap.put("title",n_title);//将高亮的字段替换原来的字体
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}

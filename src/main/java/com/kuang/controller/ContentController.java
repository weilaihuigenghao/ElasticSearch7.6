package com.kuang.controller;

import com.kuang.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;
    //到京东爬取数据
    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws Exception {
       return contentService.parseConten(keyword);
    }
    //没有高亮的业务操作
    /*@GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,@PathVariable("pageNo")int pageNo
            ,@PathVariable("pageSize")int pageSize) throws IOException {
       return contentService.searchPage(keyword,pageNo,pageSize);
    }*/
    //高亮版的业务操作
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,@PathVariable("pageNo")int pageNo
            ,@PathVariable("pageSize")int pageSize) throws IOException {
        return contentService.searchPageHightBuilder(keyword,pageNo,pageSize);
    }

}

package com.kuang.utils;

import com.kuang.entity.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.w3c.dom.html.HTMLUListElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {
    /*public static void main(String[] args) throws Exception {
        new HtmlParseUtil().parseJD("数学").forEach(System.out::println);

    }*/

    public List<Content> parseJD(String keywords) throws Exception {
        //获取请求https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword="+keywords;
        //解析网页(jsoup返回的Document就是浏览器Document对象，它可以用来做js操作)
        Document document = Jsoup.parse(new URL(url), 30000);
        //在JS中所有的方法这里都能用
        Element element = document.getElementById("J_goodsList");
        //System.out.println(element.html());
        //获取所有的li标签元素
        Elements elements = element.getElementsByTag("li");

        List<Content>goodsList=new ArrayList<>();

        //获取元素中的内容，这里el就是每一个li标签
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content=new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            goodsList.add(content);

        }
        return goodsList;
    }

}

package com.xqdd.highlightword.service;

import com.xqdd.highlightword.bean.WordItem;
import com.xqdd.highlightword.exception.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WordService {

    private static final String url = "http://dict.youdao.com/wordbook/wordlist?p=";
    private static final HttpClient client = buildHttpClient();

    private static CloseableHttpClient buildHttpClient() {
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(1000)
                        .build())
                .setMaxConnPerRoute(200)
                .setMaxConnTotal(200)
                .build();
    }

    private int getPageCount(String cookie) throws IOException, URISyntaxException {
        var htmlStr = executeGet(generateGet(url, cookie));
        var pages = Jsoup.parse(htmlStr).select(".next-page");
        if (pages.isEmpty()) {
            return 1;
        }
        var href = pages.get(1).attr("href");
        var page = StringUtils.substringBetween(href, "p=", "&");
        return Integer.parseInt(page) - 1;

    }

    private HttpGet generateGet(String url, String cookie) {
        var get = new HttpGet(url);
        get.setHeader("Cookie", cookie);
        return get;
    }

    public List<WordItem> getWordItems(String cookie) throws URISyntaxException, IOException {
        int pageCount = getPageCount(cookie);
        var list = new ArrayList<WordItem>();
        for (int i = 0; i < pageCount; i++) {
            getWordItem(executeGet(generateGet(url + i, cookie)), list);
        }
        return list;
    }

    private List<WordItem> getWordItem(String htmlStr, List<WordItem> wordItems) {
        var html = Jsoup.parse(htmlStr);
        var tbodys = html.select("tbody");
        if (!tbodys.isEmpty()) {
            var table = tbodys.get(1);
            for (Element tr : table.select("tr")) {
                var tds = tr.select("td");
                var word = tds.get(1).text().trim();
                //排除短语和中文
                if (word.contains(" ") || word.matches(".*[\\u4e00-\\u9fa5].*")) {
                    continue;
                }
                var item = new WordItem(
                        word,
                        tds.get(2).text(),
                        tds.get(3).text(),
                        tds.get(5).text()
                );
                wordItems.add(item);
            }
        }
        return wordItems;
    }

    public void deleteWord(String word, String cookie) throws URISyntaxException, IOException {
        var get = generateGet("http://dict.youdao.com/wordbook/wordlist?action=delete&word=" + word, cookie);
        get.setHeader("Referer", "http://dict.youdao.com/wordbook/wordlist");
        executeGet(get);
    }


    private String executeGet(HttpGet get) throws IOException {
        var response = client.execute(get);
        var htmlStr = EntityUtils.toString(response.getEntity());
        if (getHeader(response, "location").contains("http://account.youdao.com/login")
                || StringUtils.isNotBlank(getHeader(response, "Set-Cookie"))
                || StringUtils.contains(htmlStr, "<title>登录有道</title>")
        ) {
            throw Result.validateException(40100, "Cookie已失效，请重新登录");
        }
        return htmlStr;
    }

    private String getHeader(HttpResponse response, String name) {
        var header = response.getFirstHeader(name);
        if (header == null) {
            return "";
        }
        return header.getValue();
    }

}

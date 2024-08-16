package com.example.restea.s3.util;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class UrlParser {

    private static final String IMG_TAG = "img";
    private static final String SRC_ATTR = "src";

    @Transactional
    public Set<String> parseContentToSet(String content) {
        return parseContent(content, Collectors.toUnmodifiableSet());
    }

    @Transactional
    public List<String> parseContentToList(String content) {
        return parseContent(content, Collectors.toList());
    }

    private <T> T parseContent(String content, Collector<String, ?, T> collector) {
        // Jsoup을 이용하여 HTML 콘텐츠 파싱
        Document doc = Jsoup.parse(content);

        // 모든 img 태그 선택
        Elements imgElements = doc.select(IMG_TAG);

        // img 요소들을 순회하며 src 속성 값 추출
        return imgElements.stream()
                .map(img -> img.attr(SRC_ATTR))
                .collect(collector);
    }
}
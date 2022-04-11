package org.vi.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.vi.common.Constant;
import org.vi.common.ResBean;
import org.vi.entity.BookDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Tseng
 * @description HighLevelRestController
 * @since 2022/4/3 22:38
 */
@RestController
@RequestMapping("/high")
public class HighLevelRestController {
    private static final Logger logger = LoggerFactory.getLogger(HighLevelRestController.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 获取ES信息
     *
     * @return com.example.common.ResBean
     */
    @GetMapping("/es")
    public ResBean getEsInfo() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // SearchRequest
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        // 查询ES
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return new ResBean(HttpStatus.OK.value(), "查询成功", searchResponse);
    }

    @GetMapping("/book")
    public ResBean list(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer rows,
                        String keyword) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 分页采用简单的from + size分页，适用数据量小的，了解更多分页方式可自行查阅资料
        searchSourceBuilder.from((page - 1) * rows);
        searchSourceBuilder.size(rows);
        // 查询条件，只有查询关键字不为空才带查询条件
        if (StringUtils.isNoneBlank(keyword)) {
            QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keyword, "author", "name");
            searchSourceBuilder.query(queryBuilder);
        }
        // 排序，根据ID倒叙
//        searchSourceBuilder.sort("name", SortOrder.DESC);
//        searchSourceBuilder.sort("author", SortOrder.DESC);
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // SearchRequest
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        // 查询ES
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        // 获取总数
        Long total = hits.getTotalHits().value;
        // 遍历封装列表对象
        List<BookDto> bookDtoList = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            bookDtoList.add(JSON.parseObject(searchHit.getSourceAsString(), BookDto.class));
        }
        // 封装Map参数返回
        Map<String, Object> result = new HashMap<>(16);
        result.put("count", total);
        result.put("data", bookDtoList);
        return new ResBean(HttpStatus.OK.value(), "查询成功", result);
    }

    @GetMapping("/book/{id}")
    public ResBean getById(@PathVariable("id") String id) throws IOException {
        // GetRequest
        GetRequest getRequest = new GetRequest(Constant.INDEX, id);
        // 查询ES
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        BookDto bookDto = JSON.parseObject(getResponse.getSourceAsString(), BookDto.class);
        return new ResBean(HttpStatus.OK.value(), "查询成功", bookDto);
    }

    /**
     * 添加文档
     * @param bookDto
     * @return
     * @throws IOException
     */
    @PostMapping("/book")
    public ResBean add(@RequestBody BookDto bookDto) throws IOException {
        // IndexRequest
        IndexRequest indexRequest = new IndexRequest(Constant.INDEX);
        Long id = System.currentTimeMillis();
        bookDto.setId(id);
        String source = JSON.toJSONString(bookDto);
        indexRequest.id(id.toString()).source(source, XContentType.JSON);
        // 操作ES
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return new ResBean(HttpStatus.OK.value(), "添加成功", indexResponse);
    }

    @PutMapping("/book")
    public ResBean update(@RequestBody BookDto bookDto) throws IOException {
        // UpdateRequest
        UpdateRequest updateRequest = new UpdateRequest(Constant.INDEX, String.valueOf(bookDto.getId()));
        updateRequest.doc(JSON.toJSONString(bookDto), XContentType.JSON);
        // 操作ES
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        return new ResBean(HttpStatus.OK.value(), "修改成功", updateResponse);
    }

    /**
     * 删除文档
     * @param id
     * @return
     * @throws IOException
     */
    @DeleteMapping("/book/{id}")
    public ResBean deleteById(@PathVariable("id") String id) throws IOException {
        // DeleteRequest
        DeleteRequest deleteRequest = new DeleteRequest(Constant.INDEX, id);
        // 操作ES
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        return new ResBean(HttpStatus.OK.value(), "删除成功", deleteResponse);
    }

    @GetMapping("/getArgsTags")
    public ResBean getArgsTags(String author) throws IOException {
        SearchRequest searchRequest = new SearchRequest(Constant.INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if(StringUtils.isNotBlank(author)) {
            queryBuilder.must(QueryBuilders.multiMatchQuery(author,"author"));
            sourceBuilder.query(queryBuilder);
        }
        TermsAggregationBuilder authorAgg = AggregationBuilders.terms("group_author").field("author.keyword");
//        TermsAggregationBuilder tagAgg = AggregationBuilders.terms("group_tag").field("tag.keyword");
//        authorAgg.subAggregation(tagAgg);
        sourceBuilder.aggregation(authorAgg);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedStringTerms terms = aggregations.get("group_appName");
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        Map<String, Object> map = new HashMap<>();
        for(Terms.Bucket bucket: buckets) {
            map.put(bucket.getKey().toString(), bucket.getDocCount());
            System.out.println(bucket.getKey() + ": " + bucket.getDocCount());
        }
        return ResBean.success(map);
    }
}

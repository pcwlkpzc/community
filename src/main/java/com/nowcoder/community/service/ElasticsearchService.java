package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

/**
 * elasticsearch的业务层方法
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    /**
     * 将帖子添加到elasticsearch服务器中
     * @param post
     */
    public void saveDiscussPost(DiscussPost post){
        discussRepository.save(post);
    }

    /**
     * 从elasticsearch服务器中删除帖子
     * @param id
     */
    public void deleteDiscussPost(int id){
        discussRepository.deleteById(id);
    }

    /**
     * 从elasticsearch中查找相关的帖子
     * @param keyword 需要搜索的关键字
     * @param current 返回查询结果时，当前的页数，从0开始
     * @param limit 每页显示的条目数
     * @return
     */
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))    //构造搜索条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))     //构造排序条件，首先按照类型来排序（1为置顶，0为普通，所以要倒序排列）
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))    //构造排序条件，其次按照帖子的分数进行排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))   //构造排序条件，最后按照帖子的创建时间来排序
                .withPageable(PageRequest.of(current,limit))     //构造分页条件
                .withHighlightFields(   //构造显示时的高亮单词条件
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),  //在title中对匹配到的单词的前后加<em>标签
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")  //在content中对匹配到的单词的前后加<em>标签
                ).build();

        return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits = response.getHits();
                if(hits.getTotalHits() <= 0){
                    return null;
                }
                ArrayList<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    //处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null){
                        post.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null){
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                return new AggregatedPageImpl(list,pageable,
                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
            }
        });

    }
}

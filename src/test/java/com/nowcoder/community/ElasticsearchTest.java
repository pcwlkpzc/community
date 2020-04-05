package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    @Test
    public void testInsert(){
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussRepository.saveAll(discussMapper.selectDiscussPosts(101,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133,0,100));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134,0,100));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水。");
        discussRepository.save(post);
    }

    @Test
    public void testDelete(){
//        discussRepository.deleteById(231);//删除单条记录
        discussRepository.deleteAll();//删除所有
    }

    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))    //构造搜索条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))     //构造排序条件，首先按照类型来排序（1为置顶，0为普通，所以要倒序排列）
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))    //构造排序条件，其次按照帖子的分数进行排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))   //构造排序条件，最后按照帖子的创建时间来排序
                .withPageable(PageRequest.of(0,10))     //构造分页条件
                .withHighlightFields(   //构造显示时的高亮单词条件
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),  //在title中对匹配到的单词的前后加<em>标签
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")  //在content中对匹配到的单词的前后加<em>标签
                ).build();

        //在上面代码的底层，是调用这个方法： elasticTemplate.queryForPage(searchQuery,DiscussPost.class,SearchResultMapper);
        // 在底层其实是获取到了高亮显示的值，但是没有返回

        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalElements());//总共查到的数据
        System.out.println(page.getTotalPages());//按照当前分页条件，总共查到多少页
        System.out.println(page.getNumber());//当前处于第几页
        System.out.println(page.getSize());//每一页有最多显示多少条数据
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))    //构造搜索条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))     //构造排序条件，首先按照类型来排序（1为置顶，0为普通，所以要倒序排列）
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))    //构造排序条件，其次按照帖子的分数进行排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))   //构造排序条件，最后按照帖子的创建时间来排序
                .withPageable(PageRequest.of(0,10))     //构造分页条件
                .withHighlightFields(   //构造显示时的高亮单词条件
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),  //在title中对匹配到的单词的前后加<em>标签
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")  //在content中对匹配到的单词的前后加<em>标签
                ).build();

        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
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

        System.out.println(page.getTotalElements());//总共查到的数据
        System.out.println(page.getTotalPages());//按照当前分页条件，总共查到多少页
        System.out.println(page.getNumber());//当前处于第几页
        System.out.println(page.getSize());//每一页有最多显示多少条数据
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }
}

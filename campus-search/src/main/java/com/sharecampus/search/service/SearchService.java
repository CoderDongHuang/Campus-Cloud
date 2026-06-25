package com.sharecampus.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.search.entity.ProductDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;
    private final StringRedisTemplate redisTemplate;

    /** 商品搜索 */
    public List<ProductDoc> search(String keyword, int page, int size) throws Exception {
        Long tenantId = UserContext.getTenantId();
        // 记录热词
        redisTemplate.opsForZSet().incrementScore("hot:search", keyword, 1);

        SearchResponse<ProductDoc> response = esClient.search(s -> s
                .index("campus_product_spu")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("tenantId").value(tenantId)))
                        .must(m -> m.term(t -> t.field("status").value(1)))
                        .should(s1 -> s1.match(m -> m.field("name").query(keyword).boost(3.0f)))
                        .should(s2 -> s2.match(m -> m.field("description").query(keyword).boost(1.0f)))
                        .minimumShouldMatch("1")))
                .sort(so -> so.field(f -> f.field("salesCount").order(SortOrder.Desc)))
                .from((page - 1) * size)
                .size(size), ProductDoc.class);

        return response.hits().hits().stream()
                .map(h -> h.source())
                .collect(Collectors.toList());
    }

    /** Canal 数据同步消费者 */
    @RabbitListener(queues = MqConstants.DB_SYNC_PRODUCT_QUEUE)
    public void handleProductSync(MqMessage msg) {
        // 简化版：收到同步消息后重新索引
        log.debug("收到商品同步消息: {}", msg.getMessageId());
        // 生产环境需解析 Canal JSON → 更新 ES 索引
    }
}

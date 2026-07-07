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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;
    private final StringRedisTemplate redisTemplate;

    /** 商品搜索 */
    public List<ProductDoc> search(String keyword, int page, int size, Long tenantId) throws Exception {
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Canal binlog同步消费者 — 处理Canal原生flatMessage格式 */
    @RabbitListener(queues = "canal.data.sync.queue")
    @SuppressWarnings("unchecked")
    public void handleCanalSync(org.springframework.amqp.core.Message message) {
        try {
            String rawJson = new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
            Map<String, Object> canalMsg = objectMapper.readValue(rawJson, Map.class);
            String type = (String) canalMsg.get("type");
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) canalMsg.get("data");
            if (dataList == null || dataList.isEmpty()) return;

            for (Map<String, Object> row : dataList) {
                String spuId = String.valueOf(row.get("id"));
                if ("DELETE".equals(type)) {
                    esClient.delete(d -> d.index("campus_product_spu").id(spuId));
                    log.info("Canal-ES删除: spuId={}", spuId);
                } else {
                    ProductDoc doc = new ProductDoc();
                    doc.setSpuId(Long.valueOf(spuId));
                    doc.setName((String) row.get("name"));
                    doc.setDescription((String) row.get("description"));
                    doc.setStatus(row.get("status") != null ? Integer.valueOf(row.get("status").toString()) : 1);
                    doc.setSalesCount(row.get("sales_count") != null ? Integer.valueOf(row.get("sales_count").toString()) : 0);
                    esClient.index(i -> i.index("campus_product_spu").id(spuId).document(doc));
                    log.info("Canal-ES索引: spuId={}, name={}", spuId, doc.getName());
                }
            }
        } catch (Exception e) {
            log.error("Canal同步失败", e);
        }
    }

    /** ProductService MQ消费者 — 收到消息后索引到 ES */
    @RabbitListener(queues = MqConstants.DB_SYNC_PRODUCT_QUEUE)
    @SuppressWarnings("unchecked")
    public void handleProductSync(MqMessage msg) {
        log.info("收到数据同步消息: type={}, msgId={}", msg.getType(), msg.getMessageId());
        try {
            Map<String, Object> data = objectMapper.readValue(msg.getData(), Map.class);
            String type = (String) data.getOrDefault("type", "INSERT");
            Map<String, Object> product = (Map<String, Object>) data.get("product");
            if (product == null) {
                log.warn("product为null, 跳过: msgId={}", msg.getMessageId());
                return;
            }

            String spuId = String.valueOf(product.get("id"));
            if ("DELETE".equals(type)) {
                esClient.delete(d -> d.index("campus_product_spu").id(spuId));
                log.info("ES删除商品: spuId={}", spuId);
            } else {
                ProductDoc doc = new ProductDoc();
                doc.setSpuId(Long.valueOf(spuId));
                doc.setName((String) product.get("name"));
                doc.setDescription((String) product.get("description"));
                doc.setStatus(1);
                doc.setSalesCount(product.get("salesCount") != null ? ((Number) product.get("salesCount")).intValue() : 0);
                esClient.index(i -> i.index("campus_product_spu").id(spuId).document(doc));
                log.info("ES索引商品: spuId={}, name={}", spuId, doc.getName());
            }
        } catch (Exception e) {
            log.error("数据同步失败: msgId={}", msg.getMessageId(), e);
        }
    }
}

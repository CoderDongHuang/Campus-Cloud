package com.sharecampus.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharecampus.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT worker_id, COUNT(*) AS order_count FROM t_order WHERE worker_id IS NOT NULL GROUP BY worker_id ORDER BY order_count DESC LIMIT 10")
    List<Map<String, Object>> workerRanking();

    @Deprecated
    @Override
    default int updateById(Order entity) {
        throw new UnsupportedOperationException(
            "OrderMapper.updateById 被禁用！ShardingSphere 不允许更新分片键（order_id/tenant_id）。"
            + "请使用 LambdaUpdateWrapper 精准更新非分片键字段。"
            + "参考：OrderService.updateStatus()"
        );
    }
}

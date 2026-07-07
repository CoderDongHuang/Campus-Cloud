package com.sharecampus.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharecampus.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 * <p>
 * ⚠️ 禁止使用 updateById(Order)：Order 在 ShardingSphere 分库分表下，
 * updateById 会 SET 分片键（order_id/tenant_id），被 Proxy 拒绝。
 * 必须使用 LambdaUpdateWrapper 精准更新，见 OrderService.updateStatus()。
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * @deprecated 禁止使用！ShardingSphere 拒绝更新分片键。
     * 使用 {@code orderMapper.update(null, new LambdaUpdateWrapper<Order>()...)} 代替。
     */
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

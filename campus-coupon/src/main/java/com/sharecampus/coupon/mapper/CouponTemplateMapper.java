package com.sharecampus.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sharecampus.coupon.entity.CouponTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplate> {
    @Update("UPDATE coupon_template SET total_stock = total_stock - 1 WHERE id = #{id} AND total_stock > 0")
    int decrStock(Long id);
}

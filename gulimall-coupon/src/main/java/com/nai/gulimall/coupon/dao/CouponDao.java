package com.nai.gulimall.coupon.dao;

import com.nai.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

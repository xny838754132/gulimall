package com.nai.gulimall.coupon.dao;

import com.nai.gulimall.coupon.entity.SecKillSkuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动商品关联
 * 
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@Mapper
public interface SeckillSkuRelationDao extends BaseMapper<SecKillSkuRelationEntity> {
	
}

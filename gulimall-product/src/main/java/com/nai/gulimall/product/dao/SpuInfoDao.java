package com.nai.gulimall.product.dao;

import com.nai.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 18:16:25
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSkuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}

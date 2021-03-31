package com.nai.gulimall.product.dao;

import com.nai.gulimall.product.entity.AttrAttrGroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 18:16:25
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrGroupRelationEntity> {

    void deleteBatchRelation(@Param("entities") List<AttrAttrGroupRelationEntity> entities);
}

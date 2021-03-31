package com.nai.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.product.entity.SpuInfoEntity;
import com.nai.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 18:16:25
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上架功能
     * @param spuId
     */
    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}


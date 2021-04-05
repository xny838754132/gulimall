package com.nai.gulimall.product.vo;

import com.nai.gulimall.product.entity.SkuImagesEntity;
import com.nai.gulimall.product.entity.SkuInfoEntity;
import com.nai.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-08 20:18
 */
@ToString
@Data
public class SkuItemVo {

  SkuInfoEntity info;

  boolean hasStock = true;

  List<SkuImagesEntity> images;

  List<SkuItemSaleAttrVo> saleAttr;

  SpuInfoDescEntity desc;

  List<SpuItemAttrGroupVo> groupAttrs;
  /** 当前商品的秒杀优惠信息 */
  SecKillInfoVo secKillInfo;
}

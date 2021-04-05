package com.nai.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.dao.SkuInfoDao;
import com.nai.gulimall.product.entity.SkuImagesEntity;
import com.nai.gulimall.product.entity.SkuInfoEntity;
import com.nai.gulimall.product.entity.SpuInfoDescEntity;
import com.nai.gulimall.product.feign.SecKillFeignService;
import com.nai.gulimall.product.service.*;
import com.nai.gulimall.product.vo.SecKillInfoVo;
import com.nai.gulimall.product.vo.SkuItemSaleAttrVo;
import com.nai.gulimall.product.vo.SkuItemVo;
import com.nai.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity>
    implements SkuInfoService {
  @Autowired private SkuImagesService imagesService;

  @Autowired SpuInfoDescService descService;

  @Autowired AttrGroupService attrGroupService;

  @Autowired SkuSaleAttrValueService skuSaleAttrValueService;

  @Autowired SecKillFeignService secKillFeignService;

  @Autowired ThreadPoolExecutor executor;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<SkuInfoEntity> page =
        this.page(new Query<SkuInfoEntity>().getPage(params), new QueryWrapper<SkuInfoEntity>());

    return new PageUtils(page);
  }

  @Override
  public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
    this.baseMapper.insert(skuInfoEntity);
  }

  @Override
  public PageUtils queryPageByCondition(Map<String, Object> params) {
    QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
    String key = (String) params.get("key");
    if (!StringUtils.isEmpty(key)) {
      queryWrapper.and(
          (wrapper) -> {
            wrapper.eq("sku_id", key).or().like("sku_name", key);
          });
    }
    String catalogId = (String) params.get("catalogId");
    if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
      queryWrapper.eq("catalog_id", catalogId);
    }
    String brandId = (String) params.get("brandId");
    if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
      queryWrapper.eq("brand_id", brandId);
    }
    String min = (String) params.get("min");
    if (!StringUtils.isEmpty(min)) {
      BigDecimal minBigDecimal = new BigDecimal(min);
      queryWrapper.ge("price", minBigDecimal);
    }
    String max = (String) params.get("max");
    if (!StringUtils.isEmpty(max)) {
      try {
        BigDecimal bigDecimal = new BigDecimal(max);
        if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
          queryWrapper.le("price", bigDecimal);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);

    return new PageUtils(page);
  }

  @Override
  public List<SkuInfoEntity> getSkus(Long spuId) {
    return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
  }

  @Override
  public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
    SkuItemVo skuItemVo = new SkuItemVo();

    CompletableFuture<SkuInfoEntity> infoFuture =
        CompletableFuture.supplyAsync(
            () -> {
              // 1.sku的基本信息获取
              SkuInfoEntity info = getById(skuId);
              skuItemVo.setInfo(info);
              return info;
            },
            executor);

    CompletableFuture<Void> saleAttrFuture =
        infoFuture.thenAcceptAsync(
            res -> {
              // 3.获取spu的销售属性组合
              List<SkuItemSaleAttrVo> saleAttrVos =
                  skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
              skuItemVo.setSaleAttr(saleAttrVos);
            },
            executor);

    CompletableFuture<Void> descFuture =
        infoFuture.thenAcceptAsync(
            res -> {
              // 4.获取spu的介绍
              SpuInfoDescEntity spuInfoDescEntity = descService.getById(res.getSpuId());
              skuItemVo.setDesc(spuInfoDescEntity);
            },
            executor);

    CompletableFuture<Void> baseAttrFuture =
        infoFuture.thenAcceptAsync(
            res -> {
              // 5.获取spu的规格参数信息
              List<SpuItemAttrGroupVo> attrGroupVoList =
                  attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
              skuItemVo.setGroupAttrs(attrGroupVoList);
            },
            executor);

    CompletableFuture<Void> imageFuture =
        CompletableFuture.runAsync(
            () -> {
              // 2.sku的图片信息 pms_sku_images
              List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
              skuItemVo.setImages(images);
            },
            executor);

    // 查询当前SKU是否参与秒杀优惠
    CompletableFuture<Void> secKillFuture =
        CompletableFuture.runAsync(
            () -> {
              R secKillInfo = secKillFeignService.getSkuSecKillInfo(skuId);
              if (secKillInfo.getCode() == 0) {
                SecKillInfoVo secKillInfoVo =
                    secKillInfo.getData(new TypeReference<SecKillInfoVo>() {});
                skuItemVo.setSecKillInfo(secKillInfoVo);
              }
            },
            executor);

    // 等待所有任务都完成
    CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture, secKillFuture)
        .get();

    return skuItemVo;
  }
}

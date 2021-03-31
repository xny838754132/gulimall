package com.nai.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.to.mq.OrderTo;
import com.nai.gulimall.common.to.mq.StockLockedTo;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.ware.entity.WareSkuEntity;
import com.nai.gulimall.ware.vo.SkuHasStockVo;
import com.nai.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:58:53
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo orderTo);
}


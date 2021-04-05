package com.nai.gulimall.seckill.service;

import com.nai.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

public interface SecKillService {

  void uploadSecKillSkuLest3Days();

  List<SecKillSkuRedisTo> getCurrentSecKillSkus();

  SecKillSkuRedisTo getSkuSecKillInfo(Long skuId);

  String kill(String killId, String key, Integer num);
}

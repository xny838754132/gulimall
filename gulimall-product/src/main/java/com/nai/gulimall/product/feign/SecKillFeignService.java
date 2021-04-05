package com.nai.gulimall.product.feign;

import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.feign.fallback.SecKillFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "gulimall-seckill",fallback = SecKillFeignFallBack.class)
public interface SecKillFeignService {

  @GetMapping("/sku/sec/kill/{skuId}")
  R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);
}

package com.nai.gulimall.seckill.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

  @RequestMapping("/product/skuInfo/info/{skuId}")
  R getSkuInfo(@PathVariable("skuId") Long skuId);
}

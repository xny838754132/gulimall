package com.nai.gulimall.order.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author TheNai
 * @date 2021-03-25 22:12
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuInfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}

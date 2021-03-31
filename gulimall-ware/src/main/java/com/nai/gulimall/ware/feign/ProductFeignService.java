package com.nai.gulimall.ware.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     *
     * api/product/skuInfo/info/{skuId} 给网关发请求
     * 1).让所有请求过网关
     *  1.@FeignClient("gulimall-gateway"):给gulimall-gateway所在的机器发 请求
     * 2).直接让后台指定服务器
     *  1.@FeignClient("gulimall-product")
     *  2./product/skuInfo/info/{skuId} 直接找商品服务
     */
    @RequestMapping("/product/skuInfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}

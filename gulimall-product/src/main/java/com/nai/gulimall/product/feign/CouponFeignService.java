package com.nai.gulimall.product.feign;

import com.nai.gulimall.common.to.SkuReductionTo;
import com.nai.gulimall.common.to.SpuBoundTo;
import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 1.CouponFeignService.saveSpuBound(spuBoundTo);
     *   1).@RequestBody将这个对象转为JSON
     *   2).找到gilimall-coupon服务,给/coupon/skuladder/save发送求情.
     *   将上一步转的JSON放在请求体位置,发送请求
     *   3).对方服务收到请求.请求体里有JSON数据.
     *   (@RequestBody SpuBoundsEntity spuBounds):将请求体的JSON转为SpuBoundsEntity;
     * 只要JSON数据模型是兼容的,双方服务无需使用同一个to.
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spuBounds/save")
    R saveSpuBound(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skuFullReduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}

package com.nai.gulimall.search.feign;


import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-06 15:26
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos/{brandIds}")
    public R brandInfo(@PathVariable("brandIds") List<Long> brandIds);

    @GetMapping("/product/category/info/{catId}")
    public R catalogInfo(@PathVariable("catId") Long catId);
}

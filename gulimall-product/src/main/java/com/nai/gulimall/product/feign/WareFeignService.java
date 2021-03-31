package com.nai.gulimall.product.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 1.R设计的时候加上泛型
     * 2.直接返回想要的结果
     * 3.自己封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/wareSku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

}

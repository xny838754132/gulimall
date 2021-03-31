package com.nai.gulimall.order.feign;

import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-24 21:55
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/wareSku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareInfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/wareSku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}

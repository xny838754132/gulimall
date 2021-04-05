package com.nai.gulimall.seckill.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
  @GetMapping("/coupon/secKillSession/getLast3DaySession")
  R getLast3DaySession();
}

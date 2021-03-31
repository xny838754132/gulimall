package com.nai.gulimall.order.feign;

import com.nai.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-23 21:39
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberReceiveAddress/{memberId}/address")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}

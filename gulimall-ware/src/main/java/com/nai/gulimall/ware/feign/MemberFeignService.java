package com.nai.gulimall.ware.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author TheNai
 * @date 2021-03-24 22:31
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberReceiveAddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}

package com.nai.gulimall.auth.feign;

import com.nai.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Third part feign service.
 *
 * @author TheNai
 * @date 2021 -03-11 21:28
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    /**
     * Send code r.
     *
     * @param phone the phone
     * @param code  the code
     * @return the r
     */
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}

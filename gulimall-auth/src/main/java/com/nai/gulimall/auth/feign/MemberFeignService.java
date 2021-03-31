package com.nai.gulimall.auth.feign;

import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.SocialUser;
import com.nai.gulimall.auth.vo.UserLoginVo;
import com.nai.gulimall.auth.vo.UserRegisteredVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author TheNai
 * @date 2021-03-13 13:03
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/registry")
    R registry(@RequestBody UserRegisteredVo registryVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);

    @PostMapping("/member/member/oauth2/login")
    R login(@RequestBody SocialUser socialUser) throws Exception;

}

package com.nai.gulimall.auth.feign;

import com.nai.gulimall.auth.vo.UserLoginVo;
import com.nai.gulimall.auth.vo.UserRegisteredVo;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.SocialUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The interface Member feign service.
 *
 * @author TheNai
 * @date 2021 -03-13 13:03
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * Registry r.
     *
     * @param registryVo the registry vo
     * @return the r
     */
    @PostMapping("/member/member/registry")
    R registry(@RequestBody UserRegisteredVo registryVo);

    /**
     * Login r.
     *
     * @param loginVo the login vo
     * @return the r
     */
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo loginVo);

    /**
     * Login r.
     *
     * @param socialUser the social user
     * @return the r
     * @throws Exception the exception
     */
    @PostMapping("/member/member/oauth2/login")
    R login(@RequestBody SocialUser socialUser) throws Exception;

}

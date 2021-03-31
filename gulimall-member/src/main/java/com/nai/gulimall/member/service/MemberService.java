package com.nai.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.vo.SocialUser;
import com.nai.gulimall.member.entity.MemberEntity;
import com.nai.gulimall.member.vo.MemberLoginVo;
import com.nai.gulimall.member.vo.MemberRegistryVo;

import java.util.Map;

/**
 * 会员
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:39:37
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void registry(MemberRegistryVo registryVo);

    void checkPhoneUnique(String phone);

    void checkUserNameUnique(String userName);

    MemberEntity login(MemberLoginVo loginVo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}


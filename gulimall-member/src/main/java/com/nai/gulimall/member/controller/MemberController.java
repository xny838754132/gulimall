package com.nai.gulimall.member.controller;

import com.nai.gulimall.common.exception.BizCodeEnum;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.SocialUser;
import com.nai.gulimall.member.entity.MemberEntity;
import com.nai.gulimall.member.exception.PhoneExistException;
import com.nai.gulimall.member.exception.UserNameExistException;
import com.nai.gulimall.member.feign.CouponFeignService;
import com.nai.gulimall.member.service.MemberService;
import com.nai.gulimall.member.vo.MemberLoginVo;
import com.nai.gulimall.member.vo.MemberRegistryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:39:37
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/oauth2/login")
    public R login(@RequestBody SocialUser socialUser) throws Exception {
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCT_PASSWORD_VALID_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCT_PASSWORD_VALID_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo loginVo) {
        MemberEntity entity = memberService.login(loginVo);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCT_PASSWORD_VALID_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCT_PASSWORD_VALID_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/registry")
    public R registry(@RequestBody MemberRegistryVo registryVo) {
        try {
            memberService.registry(registryVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

package com.nai.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.nai.gulimall.common.constant.AuthServerConstant;
import com.nai.gulimall.common.exception.BizCodeEnum;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.MemberResponseVo;
import com.nai.gulimall.auth.feign.MemberFeignService;
import com.nai.gulimall.auth.feign.ThirdPartFeignService;
import com.nai.gulimall.auth.vo.UserLoginVo;
import com.nai.gulimall.auth.vo.UserRegisteredVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author TheNai
 * @date 2021-03-10 22:31
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到一个页面
     * SpringMvc viewController:将请求和页面映射过来
     */

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        //TODO 1.接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000) {
                //60S内不能再发
                return R.error(BizCodeEnum.VALID_CODE_EXCEPTION.getCode(), BizCodeEnum.VALID_CODE_EXCEPTION.getMessage());
            }
        }
        //2.验证码的再次校验  redis 存key-phone value-code  SMS:CODE:18547104524 ->456789
        String code = UUID.randomUUID().toString().substring(0, 5);
        String subString = code + "_" + System.currentTimeMillis();
        //redis缓存验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, subString, 10, TimeUnit.MINUTES);
        //防止同一个手机号在60S内,再次发送验证码
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }


    /**
     * TODO 重定向携带数据,利用session原理.将数据存放在session中,只要跳到下一个页面,取出数据以后,session里面的数据就会删掉
     * TODO 1.分布式下的session问题.
     * RedirectAttributes redirectAttributes: 模拟重定向携带数据
     *
     * @param registeredVo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("registered")
    public String registered(@Valid UserRegisteredVo registeredVo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //校验出错,转发到注册页
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:http://auth.gulimall.com/reg.html";
            //Request method 'POST' not supported
            //用户注册->registered[POST] --->转发/reg.html
        }
        //注册成功回到登录页
        //1.校验验证码
        String code = registeredVo.getCode();
        String redidCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registeredVo.getPhone());
        if (StringUtils.isNotEmpty(redidCode)) {
            if (code.equals(redidCode.split("_")[0])) {
                //删除验证码;令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registeredVo.getPhone());
                //验证码通过.
                // 真正注册 调用远程进行注册
                R registry = memberFeignService.registry(registeredVo);
                if (registry.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    //失败
                    Map<String, String> errors = new HashMap<>(1);
                    errors.put("msg", registry.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>(1);
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute==null) {
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }
    }
    @PostMapping("/login")
    public String login(UserLoginVo loginVo, RedirectAttributes redirectAttributes, HttpSession session) {
        R r = memberFeignService.login(loginVo);
        if (r.getCode() == 0) {
            //远程登录
            MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {
            });
            //成功放到session中
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}

package com.nai.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nai.gulimall.common.constant.AuthServerConstant;
import com.nai.gulimall.common.utils.HttpUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.MemberResponseVo;
import com.nai.gulimall.common.vo.SocialUser;
import com.nai.gulimall.auth.feign.MemberFeignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TheNai
 * @date 2021-03-15 22:22
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 社交登录成功回调
     *
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/oauth2.0/weibo/success")
    public String weiboLogin(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> header = new HashMap<>(0);
        Map<String, String> query = new HashMap<>(0);

        //1.根据code换取access_token
        Map<String, String> map = new HashMap<>(5);
        map.put("client_id", "3603889608");
        map.put("client_secret", "a4b188b7642067ad1649531a286bfbf0");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, map);
        //2.处理
        if (response.getStatusLine().getStatusCode() == AuthServerConstant.WEIBO_SUCCESS) {
            //获取到了accessTOKEN
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //知道当前是哪个社交用户
            //1).如果当前用户是第一次进网站,就自动注册进来(为当前社交用户生成一个会员信息账号,以后这个社交账号就对应我们指定得会员)
            //登录或者注册这个社交用户(社交用户一定要关联一个本系统得账号信息)
            R login = memberFeignService.login(socialUser);
            if (login.getCode() == 0) {
                MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {
                });
                log.info("登录成功，用户信息：{}", data.toString());
                //1.第一次使用session; 命令浏览器保存卡号 JSESSIONID这个cookie
                //以后浏览器访问哪个网站就会带上这个网站的cokiee
                //子域之间: gulimall.com  auth.gulimall.com
                //发卡的时候(指定域名为父域名),即使是子域系统发的卡,也能让父域直接使用
                //todo 1.默认发的令牌 session=dasdasas  作用域是当前域 ,(1.解决子域session共享问题)
                //todo 2.使用JSON的序列化方式来序列化对象数据到redis中
                session.setAttribute("loginUser",data);
//                servletResponse.addCookie(new Cookie("JSESSIONID",data));

                //1.session不能跨不同域名进行共享 2.分布式下session不能共享
                //3.登陆成功就跳会首页
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}

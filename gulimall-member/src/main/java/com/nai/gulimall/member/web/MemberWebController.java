package com.nai.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TheNai
 * @date 2021-03-31 20:16
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model, HttpServletRequest request) {
        //获取到支付宝给我们的所有请求数据
        //request 验证签名,如果正确可以修改
        //查出当前登录的用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders", r);
        return "orderList";
    }
}

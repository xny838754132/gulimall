package com.nai.gulimall.order.listener;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author TheNai
 * @date 2021-03-31 22:42
 */
@RestController
public class OrderPayedListener {

    @PostMapping("/payed/notify")
    public String handleAliPayed(HttpServletRequest request){
        //只要我们收到了支付宝给我们异步的通知,告诉我们订单支付成功,返回success,支付宝就不再通知
        Map<String, String[]> map = request.getParameterMap();
        System.out.println("支付宝通知到位了...数据"+map);
        return "success";
    }
}

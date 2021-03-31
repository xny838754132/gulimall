package com.nai.gulimall.order.web;

import com.nai.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author TheNai
 * @date 2021-03-22 22:16
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

//    @ResponseBody
//    @GetMapping("/test/createOrder")
//    public String createOrderTest(){
//        //订单下单成功
//        OrderEntity entity = new OrderEntity();
//        entity.setModifyTime(new Date());
//        entity.setOrderSn(UUID.randomUUID().toString());
//        //给MQ发送消息
//        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",entity);
//        return "OK";
//    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page) {
        return page;
    }
}

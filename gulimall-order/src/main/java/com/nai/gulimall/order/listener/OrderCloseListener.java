package com.nai.gulimall.order.listener;

import com.nai.gulimall.order.entity.OrderEntity;
import com.nai.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author TheNai
 * @date 2021-03-28 22:54
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

  @Autowired
  OrderService orderService;


  @RabbitHandler
  public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
    System.out.println("收到过期的订单信息,准备关闭订单" + entity.getOrderSn());
    try {
      orderService.closeOrder(entity);
      //手动调用支付宝收单

      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
      channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
    }
  }
}

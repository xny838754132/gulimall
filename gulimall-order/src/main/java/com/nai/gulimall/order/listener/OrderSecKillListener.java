package com.nai.gulimall.order.listener;

import com.nai.gulimall.common.to.mq.SecKillOrderTo;
import com.nai.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSecKillListener {

  @Autowired
  OrderService orderService;


  @RabbitHandler
  public void listener(SecKillOrderTo secKillOrderTo, Channel channel, Message message)
      throws IOException {
    log.info("准备创建秒杀单的详细信息...");
    try {
      orderService.createSecKillOrder(secKillOrderTo);
      //手动调用支付宝收单

      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
      channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
    }
  }

}

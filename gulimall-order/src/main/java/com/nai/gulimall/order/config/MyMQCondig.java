package com.nai.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TheNai
 * @date 2021-03-28 19:35
 */
@Configuration
public class MyMQCondig {

  /**
   * 容器中的组件 都会自动创建(rabbitMQ中没有的情况) 一旦创建好队列,rabbitMQ中只要有,@Bean属性发生变化也不会覆盖
   *
   * @return
   */
  @Bean
  public Queue orderDelayQueue() {
    Map<String, Object> arguments = new HashMap<>();
    // String name, boolean durable, boolean exclusive, boolean autoDelete,
    //			@Nullable Map<String, Object> arguments
    arguments.put("x-dead-letter-exchange", "order-event-exchange");
    arguments.put("x-dead-letter-routing-key", "order.release.order");
    arguments.put("x-message-ttl", 60000 * 30);
    return new Queue("order.delay.queue", true, false, false, arguments);
  }

  @Bean
  public Queue orderReleaseQueue() {
    return new Queue("order.release.order.queue", true, false, false);
  }

  @Bean
  public Exchange orderEventExchange() {
    // (String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
    return new TopicExchange("order-event-exchange", true, false);
  }

  @Bean
  public Binding orderCreateOrderBinding() {
    // String destination, DestinationType destinationType, String exchange, String routingKey,
    //			@Nullable Map<String, Object> arguments
    return new Binding(
        "order.delay.queue",
        Binding.DestinationType.QUEUE,
        "order-event-exchange",
        "order.create.order",
        null);
  }

  @Bean
  public Binding orderReleaseOrderBinding() {
    return new Binding(
        "order.release.order.queue",
        Binding.DestinationType.QUEUE,
        "order-event-exchange",
        "order.release.order",
        null);
  }

  /** 订单释放直接和库存释放的绑定 */
  @Bean
  public Binding orderReleaseOtherBinding() {
    return new Binding(
        "stock.release.stock.queue",
        Binding.DestinationType.QUEUE,
        "order-event-exchange",
        "order.release.other.#",
        null);
  }

  @Bean
  public Queue orderSecKillOrderQueue() {
    return new Queue("order.seckill.order.queue", true, false, false);
  }

  @Bean
  public Binding orderSecKillOrderBinding() {
    return new Binding(
        "order.seckill.order.queue",
        DestinationType.QUEUE,
        "order-event-exchange",
        "order.seckill.order",
        null);
  }
}

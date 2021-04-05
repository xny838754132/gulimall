package com.nai.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TheNai
 * @date 2021-03-28 20:43
 */
@Configuration
public class MyRabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange() {
        //创建交换机 String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete,
        //@Nullable Map<String, Object> arguments
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 60000*30);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Binding stockReleaseBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#", null);
    }

    @Bean
    public Binding stockLockedBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked", null);
    }



}

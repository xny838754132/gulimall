package com.nai.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author TheNai
 * @date 2021-03-21 18:26
 */
@Slf4j
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定指RabbitTemplate
     * 1.服务收到消息就回调
     * 1.spring.rabbitmq.publisher-confirm-type=correlated
     * 2.设置确认回调
     * 2.消息正确抵达队列进行回调
     * 1.spring.rabbitmq.publisher-returns=true
     * spring.rabbitmq.template.mandatory=true
     * 3.消费端确认(保证每一个消息被正确消费,此时才可以Broker删除这个消息)
     * spring.rabbitmq.listener.simple.acknowledge-mode=manual 手动接收
     * 1.默认是自动确认的,只要消息接收到,客户端会自动确认,服务端就会移除这个消息
     * 问题:
     * 我们收到很多消息,自动回复给服务器ack,只有一个消息处理成功,宕机了.发生消息丢失.
     * 消费者手动确认模式: 只要我们没有明确告诉MQ,货物已经被签收,没有ACK,消息就一直是UNACKED状态,即使Consumer宕机,消息也不会丢失,会重新变为ready状态,
     * 下一次有新的Consumer连接就发给他
     * 2.如何签收消息
     * channel.basicAck(deliveryTag, false);签收货物,业务成功完成就签收
     * channel.basicNack(deliveryTag,false,true); 拒签,业务失败就拒签
     */
    @PostConstruct //MyRabbitConfig对象创建完成以后,执行这个方法
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 1.只要消息抵达Broker,ack就等于true
             * @param correlationData 当前消息的唯一关联数据(这个是消息的唯一id)
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                /*
                1.做好消息确认机制(publisher,consumer[手动ack])
                2.每一个发送的消息,都在数据库做好记录.定期将失败的消息再次发送一遍
                 */
                log.info("confirm...correlationData:[{}],ack:[{}],cause:[{}]", correlationData, ack, cause);
            }
        });
        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没投递给指定的队列,就触发失败回调
             * @param
             *    message; 投递失败的消息详细信息
             * 	  replyCode; 回复的状态码
             * 	  replyText; 回复的文本内容
             * 	  exchange; 当时这个消息发给哪个交换机
             * 	  routingKey; 当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("Fail Message:[{}],replyCode:[{}],replyText:[{}],exchange:[{}],routingKey:[{}]",
                        message, replyCode, replyText,
                        exchange, routingKey);
            }

        });
    }
}

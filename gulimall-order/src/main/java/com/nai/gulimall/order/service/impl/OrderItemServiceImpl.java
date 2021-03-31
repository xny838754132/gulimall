package com.nai.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.order.dao.OrderItemDao;
import com.nai.gulimall.order.entity.OrderEntity;
import com.nai.gulimall.order.entity.OrderItemEntity;
import com.nai.gulimall.order.entity.OrderReturnReasonEntity;
import com.nai.gulimall.order.service.OrderItemService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service("orderItemService")
//@RabbitListener(queues = {"hello-java-queue"})
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues：声明需要监听的所有队列
     * org.springframework.amqp.core.Message
     * 参数可以写以下类型
     * 1.Message message：原生消息详细信息。头+体
     * 2.T<发送的消息的类型>  OrderReturnReasonEntity content
     * 3.Channel channel:当前传输数据的通道
     * <p>
     * Queue：可以有很多监听者。只要收到消息，队列删除消息，只能有一个人收到收到此消息
     * 场景：
     * 1).订单服务启动多个.同一个消息 只能有一个客户端收到
     * 2).只有一个消息完全处理完,方法运行结束,我们就可以接收到下一个消息
     */
//    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity content
            , Channel channel) throws InterruptedException {
        //Body:'{"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1616323008568}'
        log.info("接收到消息：{}", content);
        byte[] body = message.getBody();
        //消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();

        log.info("消息处理完成=>{}", content.getName());
        //channel内按顺序自增的.
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("deliveryTag:{}", deliveryTag);
        //签收获取,非批量签收模式
        try {
            if (deliveryTag%2==0) {
                //收货
                channel.basicAck(deliveryTag, false);
                log.info("签收了货物...{}", deliveryTag);
            }else {
                //退货 requeue=false 丢弃 requeue=true 发回服务器,服务器重新入队.
                //long deliveryTag, boolean multiple, boolean requeue
                channel.basicNack(deliveryTag,false,true);
                //long deliveryTag, boolean requeue
//                channel.basicReject();
                log.info("没有签收货物...{}", deliveryTag);
            }
        } catch (Exception e) {
            //网络中断
            log.error(e.getMessage());
        }
    }

//    @RabbitHandler
    public void receiveMessage(OrderEntity content) throws InterruptedException {
        log.info("消息处理完成=>{}", content);
    }

}
package com.nai.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.nai.gulimall.common.to.SpuBoundTo;
import com.nai.gulimall.common.to.mq.OrderTo;
import com.nai.gulimall.common.to.mq.StockDetailTo;
import com.nai.gulimall.common.to.mq.StockLockedTo;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.nai.gulimall.ware.entity.WareOrderTaskEntity;
import com.nai.gulimall.ware.feign.OrderFeignService;
import com.nai.gulimall.ware.service.WareSkuService;
import com.nai.gulimall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author TheNai
 * @date 2021-03-28 22:26
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {


    @Autowired
    WareSkuService wareSkuService;

    /**
     * 库存自动解锁
     * 下订单成功,库存锁定成功,但是接下来的业务调用失败,导致订单回滚,之前锁定的库存就要解锁
     * 订单失败:
     * 锁库存失败
     * 只要解锁库存的消息失败,一定要告诉服务器,这次解锁是失败的
     *
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的信息...");
        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handleOrderClosedRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("收到订单关闭消息,准备解锁库存...");
        try {
            wareSkuService.unLockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


}

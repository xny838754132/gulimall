package com.nai.gulimall.order.to;

import com.nai.gulimall.order.entity.OrderEntity;
import com.nai.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-25 21:32
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;
}

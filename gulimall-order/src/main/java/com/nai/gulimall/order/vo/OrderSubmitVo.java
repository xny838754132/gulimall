package com.nai.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author TheNai
 * @date 2021-03-25 20:47
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {

    /**
     * 收获地址id
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;

    /*
     * 无需提交需要购买的商品,去购物车再次获取
        优惠 发票等
     */

    /**
     * 防重令牌
     */
    private String orderToken;

    /**
     * 应付价格 验价
     */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;
    //用户相关信息 都在session中,直接去session中取
}

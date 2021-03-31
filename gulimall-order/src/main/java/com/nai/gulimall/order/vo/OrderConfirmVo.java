package com.nai.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author TheNai
 * @date 2021-03-22 23:37
 */

public class OrderConfirmVo {

    @Setter @Getter
    Map<Long,Boolean> stocks;
    /**
     * 用户的收货地址列表
     */
    @Setter
    @Getter
    private List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    @Setter
    @Getter
    private List<OrderItemVo> items;

    @Setter
    private Integer count;

    public Integer getCount() {
        count = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }


    //发票记录...

    /**
     * 优惠卷信息
     */
    @Setter
    @Getter
    private Integer integration;

    @Getter
    @Setter
    String orderToken;

    @Setter
    private BigDecimal total;

    public BigDecimal getTotal() {
        total = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                total = total.add(multiply);
            }
        }
        return total;
    }

    /**
     * 应付价格
     */
    @Setter
    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        payPrice = getTotal();
        return payPrice;
    }
}

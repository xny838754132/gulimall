package com.nai.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-25 22:55
 */
@Data
public class WareSkuLockVo {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 需要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;


}

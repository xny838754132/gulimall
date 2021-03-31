package com.nai.gulimall.order.vo;

import com.nai.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author TheNai
 * @date 2021-03-25 21:06
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /**
     * 状态码
     * 0-成功
     */
    private Integer code;
}

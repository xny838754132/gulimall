package com.nai.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author TheNai
 * @date 2021-03-25 21:42
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}

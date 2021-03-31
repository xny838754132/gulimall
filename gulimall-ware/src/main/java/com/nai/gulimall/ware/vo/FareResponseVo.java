package com.nai.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * @author TheNai
 * @date 2021-03-24 23:05
 */
@SuperBuilder(toBuilder = true)
@Data
public class FareResponseVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}

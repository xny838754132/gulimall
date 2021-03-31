package com.nai.gulimall.ware.vo;

import lombok.Data;

/**
 * @author TheNai
 * @date 2021-03-25 22:58
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}

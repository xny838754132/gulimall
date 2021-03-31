package com.nai.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author TheNai
 * @date 2021-03-09 22:22
 */
@Data
@ToString
public class AttrValueWithSkuIdVo {
    private String attrValue;

    private String skuIds;
}

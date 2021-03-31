package com.nai.gulimall.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-28 21:22
 */
@Data
public class StockLockedTo {

    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作单详情
     */
    private StockDetailTo detail;

}


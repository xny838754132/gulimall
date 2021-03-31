package com.nai.gulimall.common.exception;

/**
 * @author TheNai
 * @date 2021-03-26 21:40
 */
public class NoStockException extends RuntimeException {

    private Long skuId;

    private String msg;

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + "没有足够得库存了");
    }

    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}

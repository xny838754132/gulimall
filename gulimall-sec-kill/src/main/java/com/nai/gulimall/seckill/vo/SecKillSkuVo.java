package com.nai.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillSkuVo {
  /** id */
  private Long id;
  /** 活动id */
  private Long promotionId;
  /** 活动场次id */
  private Long promotionSessionId;
  /** 商品id */
  private Long skuId;
  /** 秒杀价格 */
  private BigDecimal secKillPrice;
  /** 秒杀总量 */
  private BigDecimal secKillCount;
  /** 每人限购数量 */
  private BigDecimal secKillLimit;
  /** 排序 */
  private Integer secKillSort;
}

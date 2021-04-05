package com.nai.gulimall.product.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SecKillInfoVo implements Serializable {
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
  /** 当前商品秒杀的开始时间 */
  private Long startTime;
  /** 当前商品秒杀的结束时间 */
  private Long endTime;
  /** 商品秒杀随机码 */
  private String randomCode;
}

package com.nai.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀活动商品关联
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@Data
@TableName("sms_sec_kill_sku_relation")
public class SecKillSkuRelationEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  @TableId private Long id;
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

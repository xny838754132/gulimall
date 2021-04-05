package com.nai.gulimall.common.to.mq;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SecKillOrderTo {
  /** 订单号 */
  private String orderSn;
  /** 活动场次id */
  private Long promotionSessionId;
  /** 商品id */
  private Long skuId;
  /** 秒杀价格 */
  private BigDecimal secKillPrice;
  /** 购买数量 */
  private Integer num;
  /** 会员id */
  private Long memberId;
}

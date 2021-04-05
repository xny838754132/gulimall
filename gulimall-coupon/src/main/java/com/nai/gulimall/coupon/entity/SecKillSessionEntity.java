package com.nai.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 秒杀活动场次
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@Data
@TableName("sms_sec_kill_session")
public class SecKillSessionEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  /** id */
  @TableId private Long id;
  /** 场次名称 */
  private String name;
  /** 每日开始时间 */
  private Date startTime;
  /** 每日结束时间 */
  private Date endTime;
  /** 启用状态 */
  private Integer status;
  /** 创建时间 */
  private Date createTime;

  @TableField(exist = false)
  private List<SecKillSkuRelationEntity> relationSkus;
}

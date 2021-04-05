package com.nai.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.coupon.entity.SecKillPromotionEntity;

import java.util.Map;

/**
 * 秒杀活动
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
public interface SecKillPromotionService extends IService<SecKillPromotionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


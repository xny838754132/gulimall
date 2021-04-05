package com.nai.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.coupon.entity.SecKillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
public interface SecKillSessionService extends IService<SecKillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

  List<SecKillSessionEntity> getLast3DaySession();
}


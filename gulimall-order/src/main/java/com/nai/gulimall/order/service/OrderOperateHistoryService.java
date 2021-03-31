package com.nai.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:44:51
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


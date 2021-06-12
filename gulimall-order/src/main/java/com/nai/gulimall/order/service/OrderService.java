package com.nai.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.to.mq.SecKillOrderTo;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.order.entity.OrderEntity;
import com.nai.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author TheNai
 * @email TheNai @gmail.com
 * @date 2021 -02-06 22:44:51
 */
public interface OrderService extends IService<OrderEntity> {

  /**
   * Query page page utils.
   *
   * @param params the params
   * @return the page utils
   */
  PageUtils queryPage(Map<String, Object> params);

  /**
   * 订单确认页 返回需要用的数据
   *
   * @return order confirm vo
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

  /**
   * 下单
   *
   * @param orderSubmitVo the order submit vo
   * @return submit order response vo
   */
  SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

  /**
   * Gets order by order sn.
   *
   * @param orderSn the order sn
   * @return the order by order sn
   */
  OrderEntity getOrderByOrderSn(String orderSn);

  /**
   * Close order.
   *
   * @param entity the entity
   */
  void closeOrder(OrderEntity entity);

  /**
   * 获取当前订单的支付信息
   *
   * @param orderSn the order sn
   * @return order pay
   */
  PayVo getOrderPay(String orderSn);

  /**
   * Query page with item page utils.
   *
   * @param params the params
   * @return the page utils
   */
  PageUtils queryPageWithItem(Map<String, Object> params);

  /**
   * Handle pay result string.
   *
   * @param vo the vo
   * @return the string
   */
  String handlePayResult(PayAsyncVo vo);

  /**
   * Create sec kill order.
   *
   * @param secKillOrderTo the sec kill order to
   */
  void createSecKillOrder(SecKillOrderTo secKillOrderTo);
}

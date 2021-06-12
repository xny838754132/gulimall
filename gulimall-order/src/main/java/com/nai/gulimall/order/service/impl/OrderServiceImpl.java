package com.nai.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.exception.NoStockException;
import com.nai.gulimall.common.to.mq.OrderTo;
import com.nai.gulimall.common.to.mq.SecKillOrderTo;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.MemberResponseVo;
import com.nai.gulimall.order.constant.OrderConstant;
import com.nai.gulimall.order.constant.RedisConstant;
import com.nai.gulimall.order.dao.OrderDao;
import com.nai.gulimall.order.entity.OrderEntity;
import com.nai.gulimall.order.entity.OrderItemEntity;
import com.nai.gulimall.order.entity.PaymentInfoEntity;
import com.nai.gulimall.order.enume.OrderStatusEnum;
import com.nai.gulimall.order.feign.CartFeignService;
import com.nai.gulimall.order.feign.MemberFeignService;
import com.nai.gulimall.order.feign.ProductFeignService;
import com.nai.gulimall.order.feign.WmsFeignService;
import com.nai.gulimall.order.intercepetor.LoginUserInterceptor;
import com.nai.gulimall.order.service.OrderItemService;
import com.nai.gulimall.order.service.OrderService;
import com.nai.gulimall.order.service.PaymentInfoService;
import com.nai.gulimall.order.to.OrderCreateTo;
import com.nai.gulimall.order.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

  private ThreadLocal<OrderSubmitVo> orderSubmitThreadLocal = new ThreadLocal<>();

  @Autowired RabbitTemplate rabbitTemplate;

  @Autowired OrderItemService orderItemService;

  @Autowired MemberFeignService memberFeignService;

  @Autowired ProductFeignService productFeignService;

  @Autowired CartFeignService cartFeignService;

  @Autowired WmsFeignService wmsFeignService;

  @Autowired StringRedisTemplate redisTemplate;

  @Autowired PaymentInfoService paymentInfoService;

  @Autowired ThreadPoolExecutor executor;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<OrderEntity> page =
        this.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>());

    return new PageUtils(page);
  }

  @Override
  public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
    OrderConfirmVo confirmVo = new OrderConfirmVo();
    // 展示订单确认的数据
    MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
    // 获取之前的请求
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    System.out.println("主线程..." + Thread.currentThread().getId());
    // 1.远程查询所有的收获地址列表
    CompletableFuture<Void> getAddressFuture =
        CompletableFuture.runAsync(
            () -> {
              System.out.println("address线程..." + Thread.currentThread().getId());
              // 每一个线程都来共享之前的请求数据
              RequestContextHolder.setRequestAttributes(requestAttributes);
              List<MemberAddressVo> address =
                  memberFeignService.getAddress(memberResponseVo.getId());
              confirmVo.setAddress(address);
            },
            executor);
    // 2.远程查询购物车所有选中的购物项
    CompletableFuture<Void> cartFuture =
        CompletableFuture.runAsync(
                () -> {
                  System.out.println("cart线程..." + Thread.currentThread().getId());
                  RequestContextHolder.setRequestAttributes(requestAttributes);
                  List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
                  confirmVo.setItems(items);
                  // feign在远程调用之前要构造请求,调用很多拦截器
                },
                executor)
            .thenRunAsync(
                () -> {
                  List<OrderItemVo> items = confirmVo.getItems();
                  List<Long> skuIds =
                      items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                  R r = wmsFeignService.getSkusHasStock(skuIds);
                  List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>() {});
                  if (data != null) {
                    Map<Long, Boolean> map =
                        data.stream()
                            .collect(
                                Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                    confirmVo.setStocks(map);
                  }
                },
                executor);
    // 3.查询用户积分
    Integer integration = memberResponseVo.getIntegration();
    confirmVo.setIntegration(integration);
    // 4.其他数据自动计算

    // 5.防重令牌
    String token = UUID.randomUUID().toString().replace("-", "");
    redisTemplate
        .opsForValue()
        .set(
            OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(),
            token,
            30,
            TimeUnit.MINUTES);
    confirmVo.setOrderToken(token);
    CompletableFuture.allOf(getAddressFuture, cartFuture).get();
    return confirmVo;
  }

  /**
   * 本地事务,在分布式系统下,只能控制住自己得回滚,控制不了其他服务得回滚 分布式事务:最大原因.网络问题+分布式机器. rollbackFor = Exception.class
   * isolation = Isolation.REPEATABLE_READ propagation = Propagation.REQUIRED 事务使用代理对象控制的 同一个对象内
   * 事务方法互调默认失效,原因,绕过了代理对象
   *
   * @param orderSubmitVo
   * @return @GlobalTransactional不适用高并发场景
   */
  //    @GlobalTransactional
  @Transactional(rollbackFor = Exception.class)
  @Override
  public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
    orderSubmitThreadLocal.set(orderSubmitVo);
    SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
    MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
    responseVo.setCode(0);
    // 创建订单,验证令牌,价格,锁库存
    // 1.验证令牌[令牌得对比和删除必须保证原子性]
    // 0令牌失败  -  1删除成功
    String orderToken = orderSubmitVo.getOrderToken();
    // 原子验证令牌和删除令牌
    Long result =
        redisTemplate.execute(
            new DefaultRedisScript<>(RedisConstant.SCRIPT, Long.class),
            Collections.singletonList(
                OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
            orderToken);
    assert result != null;
    if (result == 0L) {
      // 令牌验证失败令牌验证成功
      responseVo.setCode(1);
      return responseVo;
    } else {
      // 令牌验证成功
      // 1.创建订单,订单项等信息
      OrderCreateTo order = createTo();
      // 2.验证价格
      BigDecimal payAmount = order.getOrder().getPayAmount();
      BigDecimal payPrice = orderSubmitVo.getPayPrice();
      if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
        // 金额对比成功
        // TODO 3.保存订单
        saveOrder(order);
        // 4.库存锁定,只要有异常回滚订单数据.
        // 订单号 , 所有订单项信息(skuId,num,skuName)
        WareSkuLockVo lockVo = new WareSkuLockVo();
        lockVo.setOrderSn(order.getOrder().getOrderSn());
        List<OrderItemVo> orderItemVos =
            order.getOrderItems().stream()
                .map(
                    item -> {
                      OrderItemVo orderItemVo = new OrderItemVo();
                      orderItemVo.setSkuId(item.getSkuId());
                      orderItemVo.setCount(item.getSkuQuantity());
                      orderItemVo.setTitle(item.getSkuName());
                      return orderItemVo;
                    })
                .collect(Collectors.toList());
        lockVo.setLocks(orderItemVos);
        // 远程锁库存
        // 库存成功了,但是网络原因超时了,库存回滚,订单不回滚

        // 为了保证高并发,库存服务自己回滚.可以发送消息给库存服务;
        // 库存服务本身自动解锁模式,参与消息队列
        R r = wmsFeignService.orderLockStock(lockVo);
        if (r.getCode() == 0) {
          // 锁定成功了
          responseVo.setOrder(order.getOrder());
          // TODO 远程扣减积分
          // 订单回滚,订单不回滚
          // TODO 订单创建成功,发送消息给MQ
          rabbitTemplate.convertAndSend(
              "order-event-exchange", "order.create.order", order.getOrder());
          return responseVo;
        } else {
          // 锁定失败了
          String msg = (String) r.get("msg");
          throw new NoStockException(msg);
        }
      } else {
        responseVo.setCode(2);
        return responseVo;
      }
    }
  }

  /**
   * 保存订单数据
   *
   * @param order
   */
  private void saveOrder(OrderCreateTo order) {
    OrderEntity orderEntity = order.getOrder();
    orderEntity.setModifyTime(new Date());
    this.save(orderEntity);
    List<OrderItemEntity> orderItems = order.getOrderItems();
    orderItemService.saveBatch(orderItems);
  }

  private OrderCreateTo createTo() {
    OrderCreateTo orderCreateTo = new OrderCreateTo();
    // 1.生成订单号
    String orderSn = IdWorker.getTimeId();
    // 创建订单号
    OrderEntity orderEntity = buildOrder(orderSn);
    // 2.准备所有得订单项信息
    List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
    // 3.验证价格-计算价格相关
    assert orderItemEntities != null;
    computePrice(orderEntity, orderItemEntities);
    orderCreateTo.setOrder(orderEntity);
    orderCreateTo.setOrderItems(orderItemEntities);
    return orderCreateTo;
  }

  private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
    // 1.订单价格相关
    BigDecimal total = new BigDecimal("0.0");
    BigDecimal coupon = new BigDecimal("0.0");
    BigDecimal integration = new BigDecimal("0.0");
    BigDecimal promotion = new BigDecimal("0.0");
    BigDecimal giftIntegration = new BigDecimal("0.0");
    BigDecimal giftGrowth = new BigDecimal("0.0");
    // 订单的总额,叠加每一个订单项的总额数据
    for (OrderItemEntity entity : orderItemEntities) {
      total = total.add(entity.getRealAmount());
      coupon = coupon.add(entity.getCouponAmount());
      integration = integration.add(entity.getIntegrationAmount());
      promotion = promotion.add(entity.getPromotionAmount());
      giftIntegration = giftIntegration.add(new BigDecimal(entity.getGiftIntegration().toString()));
      giftGrowth = giftGrowth.add(new BigDecimal(entity.getGiftGrowth().toString()));
    }
    orderEntity.setTotalAmount(total);
    // 设置应付总额
    orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
    orderEntity.setPromotionAmount(promotion);
    orderEntity.setIntegrationAmount(integration);
    orderEntity.setCouponAmount(coupon);
    // 设置积分等信息
    orderEntity.setIntegration(giftIntegration.intValue());
    orderEntity.setGrowth(giftGrowth.intValue());
    orderEntity.setDeleteStatus(0);
  }

  private OrderEntity buildOrder(String orderSn) {
    OrderEntity entity = new OrderEntity();
    entity.setOrderSn(orderSn);
    entity.setMemberId(LoginUserInterceptor.loginUser.get().getId());
    // 获取收货地址信息
    OrderSubmitVo orderSubmitVo = orderSubmitThreadLocal.get();
    R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
    FareVo fareResponse = fare.getData(new TypeReference<FareVo>() {});
    // 设置运费信息
    entity.setFreightAmount(fareResponse.getFare());
    // 设置收货人信息
    entity.setReceiverCity(fareResponse.getAddress().getCity());
    entity.setReceiverDetailAddress(fareResponse.getAddress().getDetailAddress());
    entity.setReceiverName(fareResponse.getAddress().getName());
    entity.setReceiverPhone(fareResponse.getAddress().getPhone());
    entity.setReceiverPostCode(fareResponse.getAddress().getPostCode());
    entity.setReceiverProvince(fareResponse.getAddress().getProvince());
    entity.setReceiverRegion(fareResponse.getAddress().getRegion());
    // 设置订单状态
    entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
    entity.setAutoConfirmDay(7);
    return entity;
  }

  /**
   * 构建所有订单项
   *
   * @return
   */
  private List<OrderItemEntity> buildOrderItems(String orderSn) {
    // 最后确定每个购物项的价格
    List<OrderItemVo> orderItemVos = cartFeignService.getCurrentUserCartItems();
    if (CollectionUtils.isNotEmpty(orderItemVos)) {
      return orderItemVos.stream()
          .map(
              orderItemVo -> {
                OrderItemEntity orderItemEntity = buildOrderItem(orderItemVo);
                if (StringUtils.isNotEmpty(orderSn)) {
                  orderItemEntity.setOrderSn(orderSn);
                }
                return orderItemEntity;
              })
          .collect(Collectors.toList());
    }
    return null;
  }

  /**
   * 构建某一个订单项
   *
   * @param orderItemVo
   * @return
   */
  private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
    OrderItemEntity itemEntity = new OrderItemEntity();
    // 1.订单信息:订单号
    // 2.商品的spu信息
    Long skuId = orderItemVo.getSkuId();
    R r = productFeignService.getSpuInfoBySkuId(skuId);
    SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {});
    itemEntity.setSpuId(data.getId());
    itemEntity.setSpuBrand(data.getBrandId().toString());
    itemEntity.setSpuName(data.getSpuName());
    itemEntity.setCategoryId(data.getCatalogId());
    // 3.商品的sku信息
    itemEntity.setSkuId(orderItemVo.getSkuId());
    itemEntity.setSkuName(orderItemVo.getTitle());
    itemEntity.setSkuPic(orderItemVo.getImage());
    itemEntity.setSkuPrice(orderItemVo.getPrice());
    String skuAttr = StringUtils.join(orderItemVo.getSkuAttr(), ";");
    itemEntity.setSkuAttrsValues(skuAttr);
    itemEntity.setSkuQuantity(orderItemVo.getCount());
    // 4.优惠信息[不做]
    // 5.积分信息
    itemEntity.setGiftGrowth(
        orderItemVo
            .getPrice()
            .multiply(new BigDecimal(orderItemVo.getCount().toString()))
            .intValue());
    itemEntity.setGiftIntegration(
        orderItemVo
            .getPrice()
            .multiply(new BigDecimal(orderItemVo.getCount().toString()))
            .intValue());
    // 6.设置订单项的价格信息
    itemEntity.setPromotionAmount(new BigDecimal("0.0"));
    itemEntity.setCouponAmount(new BigDecimal("0.0"));
    itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
    // 当前订单项的实际金额 总额减去各种优惠
    BigDecimal origin =
        itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
    BigDecimal realPrice =
        origin
            .subtract(itemEntity.getPromotionAmount())
            .subtract(itemEntity.getCouponAmount())
            .subtract(itemEntity.getIntegrationAmount());
    itemEntity.setRealAmount(realPrice);
    return itemEntity;
  }

  @Override
  public OrderEntity getOrderByOrderSn(String orderSn) {
    return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
  }

  @Override
  public void closeOrder(OrderEntity entity) {
    // 查询当前订单的最新状态
    OrderEntity orderEntity = this.getById(entity.getId());
    // 关单
    if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
      OrderEntity update = new OrderEntity();
      update.setId(entity.getId());
      update.setStatus(OrderStatusEnum.CANCLED.getCode());
      this.updateById(update);
      // 发给MQ一个
      OrderTo orderTo = new OrderTo();
      BeanUtils.copyProperties(orderEntity, orderTo);
      try {
        // 保证消息一定会发送出去,每一个消息都可以做日志记录(给数据库保存每一个消息的消息信息)
        // 定期扫描数据库,将失败的消息再发送一次
        rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
      } catch (Exception e) {
        // 将没发送成功的消息进行重试发送

      }
    }
  }

  @Override
  public PayVo getOrderPay(String orderSn) {
    PayVo payVo = new PayVo();
    OrderEntity order = this.getOrderByOrderSn(orderSn);
    List<OrderItemEntity> orderItems =
        orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
    OrderItemEntity orderItemEntity = orderItems.get(0);
    BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
    payVo.setTotal_amount(bigDecimal.toString());
    payVo.setOut_trade_no(order.getOrderSn());
    payVo.setSubject(orderItemEntity.getSkuName());
    payVo.setBody(orderItemEntity.getSkuAttrsValues());
    return payVo;
  }

  /**
   * 分页查询当前登录用户的所有订单
   *
   * @param params
   * @return
   */
  @Override
  public PageUtils queryPageWithItem(Map<String, Object> params) {
    MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
    IPage<OrderEntity> page =
        this.page(
            new Query<OrderEntity>().getPage(params),
            new QueryWrapper<OrderEntity>()
                .eq("member_id", memberResponseVo.getId())
                .orderByDesc("id"));
    List<OrderEntity> orderEntities =
        page.getRecords().stream()
            .peek(
                order -> {
                  List<OrderItemEntity> itemEntities =
                      orderItemService.list(
                          new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
                  order.setItemEntities(itemEntities);
                })
            .collect(Collectors.toList());
    page.setRecords(orderEntities);
    System.out.println(JSON.toJSONString(orderEntities));
    return new PageUtils(page);
  }

  /**
   * 处理支付宝的支付结果
   *
   * @param vo
   * @return
   */
  @Override
  public String handlePayResult(PayAsyncVo vo) {
    // 1.保存交易流水
    PaymentInfoEntity infoEntity = new PaymentInfoEntity();
    infoEntity.setAlipayTradeNo(vo.getTrade_no());
    infoEntity.setOrderSn(vo.getOut_trade_no());
    infoEntity.setPaymentStatus(vo.getTrade_status());
    infoEntity.setCallbackTime(vo.getNotify_time());
    paymentInfoService.save(infoEntity);
    // 2.修改订单的状态信息
    if ("TRADE_SUCCESS".equals(vo.getTrade_status())
        || "TRADE_FINISHED".equals(vo.getTrade_status())) {
      // 支付成功状态
      String outTradeNo = vo.getOut_trade_no();
      this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
    }
    return "success";
  }

  @Override
  public void createSecKillOrder(SecKillOrderTo secKillOrderTo) {
    // 保存订单信息
    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setOrderSn(secKillOrderTo.getOrderSn());
    orderEntity.setMemberId(secKillOrderTo.getMemberId());
    orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
    BigDecimal multiply =
        secKillOrderTo.getSecKillPrice().multiply(new BigDecimal("" + secKillOrderTo.getNum()));
    orderEntity.setPayAmount(multiply);
    this.save(orderEntity);
    // 保存订单项信息
    OrderItemEntity orderItemEntity = new OrderItemEntity();
    orderItemEntity.setOrderSn(secKillOrderTo.getOrderSn());
    orderItemEntity.setRealAmount(multiply);
    orderItemEntity.setSkuQuantity(secKillOrderTo.getNum());
    // TODO 获取当前SKU的详细信息进行设置
    R spuInfo = productFeignService.getSpuInfoBySkuId(secKillOrderTo.getSkuId());
    if (spuInfo.getCode() == 0) {
      SpuInfoVo spuInfoData = spuInfo.getData(new TypeReference<SpuInfoVo>() {});
      orderItemEntity.setSpuName(spuInfoData.getSpuName());
      orderItemEntity.setCategoryId(spuInfoData.getCatalogId());
    }
    orderItemService.save(orderItemEntity);
  }
}

package com.nai.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nai.gulimall.common.to.mq.SecKillOrderTo;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.common.vo.MemberResponseVo;
import com.nai.gulimall.seckill.feign.CouponFeignService;
import com.nai.gulimall.seckill.feign.ProductFeignService;
import com.nai.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.nai.gulimall.seckill.service.SecKillService;
import com.nai.gulimall.seckill.to.SecKillSkuRedisTo;
import com.nai.gulimall.seckill.vo.SecKillSessionsWithSkus;
import com.nai.gulimall.seckill.vo.SkuInfoVo;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SecKillServiceImpl implements SecKillService {

  @Autowired CouponFeignService couponFeignService;

  @Autowired StringRedisTemplate redisTemplate;

  @Autowired ProductFeignService productFeignService;

  @Autowired RedissonClient redissonClient;

  @Autowired RabbitTemplate rabbitTemplate;

  private final String SESSIONS_CACHE_PREFIX = "SEC:KILL:SESSIONS:";

  private final String SKU_KILL_CACHE_PREFIX = "SEC:KILL:SKUS";

  private final String SKU_STOCK_SEMAPHORE = "SEC:KILL:STOCK:";

  @Override
  public void uploadSecKillSkuLest3Days() {
    // 1.去扫描最近三天需要参与秒杀的活动
    R session = couponFeignService.getLast3DaySession();
    if (session.getCode() == 0) {
      List<SecKillSessionsWithSkus> sessions =
          session.getData(new TypeReference<List<SecKillSessionsWithSkus>>() {});
      // 缓存活动信息
      saveSessionInfo(sessions);
      // 缓存活动的关联商品信息
      saveSessionSkuInfo(sessions);
    }
  }

  private void saveSessionInfo(List<SecKillSessionsWithSkus> sessions) {
    if (CollectionUtils.isEmpty(sessions)) {
      return;
    }
    sessions.forEach(
        session -> {
          long startTime = session.getStartTime().getTime();
          long endTime = session.getEndTime().getTime();
          String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
          Boolean hasKey = redisTemplate.hasKey(key);
          assert hasKey != null;
          if (!hasKey) {
            List<String> collect =
                session.getRelationSkus().stream()
                    .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId())
                    .collect(Collectors.toList());
            // 缓存活动信息
            redisTemplate.opsForList().leftPushAll(key, collect);
          }
        });
  }

  private void saveSessionSkuInfo(List<SecKillSessionsWithSkus> sessions) {
    sessions.forEach(
        session -> {
          // 准备hash操作
          BoundHashOperations<String, Object, Object> ops =
              redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
          session
              .getRelationSkus()
              .forEach(
                  secKillSkuVo -> {
                    String token = UUID.randomUUID().toString().replace("_", "");
                    Boolean existKey =
                        ops.hasKey(
                            secKillSkuVo.getPromotionSessionId() + "_" + secKillSkuVo.getSkuId());
                    assert existKey != null;
                    if (!existKey) {
                      // 缓存商品
                      SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                      // 1.sku的基本信息
                      R skuInfo = productFeignService.getSkuInfo(secKillSkuVo.getSkuId());
                      if (skuInfo.getCode() == 0) {
                        SkuInfoVo info =
                            skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                        redisTo.setSkuInfo(info);
                      }
                      // 2.sku的秒杀信息
                      BeanUtils.copyProperties(secKillSkuVo, redisTo);
                      // 3.设置当前商品的秒杀时间信息
                      redisTo.setStartTime(session.getStartTime().getTime());
                      redisTo.setEndTime(session.getEndTime().getTime());
                      // 4.设置商品的随机码
                      redisTo.setRandomCode(token);
                      String jsonString = JSON.toJSONString(redisTo);
                      ops.put(
                          secKillSkuVo.getPromotionSessionId() + "_" + secKillSkuVo.getSkuId(),
                          jsonString);
                      // 如果当前这个场次的商品的库存信息,已经上架就不需要上架
                      // 5.引入分布式的信号量--限流
                      RSemaphore semaphore =
                          redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                      semaphore.trySetPermits(secKillSkuVo.getSecKillCount().intValue());
                      semaphore.expireAt(session.getEndTime());
                    }
                  });
        });
  }

  public List<SecKillSkuRedisTo> blockHandler(BlockException e) {
    log.error("getCurrentSecKillSkusResource被限流了");
    return null;
  }

  /**
   * blockHandler针对指定方法降级被调用
   * fallback针对抛出异常后的处理逻辑
   * @return
   */
  @SentinelResource(value = "getCurrentSecKillSkusResource", blockHandler = "blockHandler")
  @Override
  public List<SecKillSkuRedisTo> getCurrentSecKillSkus() {
    // 1.确定当前时间属于哪个秒杀场次
    long time = new Date().getTime();
    try (Entry entry = SphU.entry("secKillSkus")) {
      Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
      assert keys != null;
      for (String key : keys) {
        String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
        String[] str = replace.split("_");
        long start = Long.parseLong(str[0]);
        long end = Long.parseLong(str[1]);
        if (time >= start && time <= end) {
          // 2.获取这个秒杀场次需要的所有商品信息
          List<String> range = redisTemplate.opsForList().range(key, -100, 100);
          BoundHashOperations<String, String, String> hashOps =
              redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
          assert range != null;
          List<String> list = hashOps.multiGet(range);
          if (!CollectionUtils.isEmpty(list)) {
            return list.stream()
                .map(
                    item -> {
                      // 当前秒杀开始了需要随机码
                      return JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                    })
                .collect(Collectors.toList());
          }
          break;
        }
      }
    } catch (BlockException e) {
      log.error("资源被限流:{}", e.getMessage());
    }

    return null;
  }

  @Override
  public SecKillSkuRedisTo getSkuSecKillInfo(Long skuId) {
    // 1.找到所有需要参与秒杀的商品的key信息
    BoundHashOperations<String, String, String> hashOps =
        redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
    Set<String> keys = hashOps.keys();
    if (!CollectionUtils.isEmpty(keys)) {
      String reg = "\\d_" + skuId;
      for (String key : keys) {
        if (Pattern.matches(reg, key)) {
          String json = hashOps.get(key);
          SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
          // 随机码
          if (redisTo != null) {
            long current = new Date().getTime();
            if (current < redisTo.getStartTime() && current > redisTo.getEndTime()) {
              redisTo.setRandomCode(null);
            }
            return redisTo;
          }
        }
      }
    }
    return null;
  }

  /**
   * TODO 上架秒杀商品的时候,每一个数据都有过期时间 秒杀后续的流程,简化了收货地址等信息
   *
   * @param killId
   * @param key
   * @param num
   * @return
   */
  @Override
  public String kill(String killId, String key, Integer num) {
    long start = System.currentTimeMillis();
    MemberResponseVo responseVo = LoginUserInterceptor.loginUser.get();
    // 1.获取当前秒杀商品的详细信息
    BoundHashOperations<String, String, String> hashOps =
        redisTemplate.boundHashOps(SKU_KILL_CACHE_PREFIX);
    String json = hashOps.get(killId);
    if (StringUtils.isEmpty(json)) {
      return null;
    } else {
      SecKillSkuRedisTo redisTo = JSON.parseObject(json, new TypeReference<SecKillSkuRedisTo>() {});
      // 合法性校验
      Long startTime = redisTo.getStartTime();
      Long endTime = redisTo.getEndTime();
      long time = new Date().getTime();
      // 校验时间的合法性
      long ttl = endTime - startTime;
      if (time >= startTime && time <= endTime) {
        // 校验随机码和商品id是否正确
        String randomCode = redisTo.getRandomCode();
        String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
        if (randomCode.equals(key) && killId.equals(skuId)) {
          // 验证购物数量是否合理
          if (num <= redisTo.getSecKillLimit().intValue()) {
            // 验证这个人是否已经购买过.幂等性;如果只要秒杀成功,就去redis占位-->userId_sessionId_skuId
            String redisKey = responseVo.getId() + skuId;
            // 自动过期->当前场次的结束时间
            Boolean aBoolean =
                redisTemplate
                    .opsForValue()
                    .setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
            assert aBoolean != null;
            if (aBoolean) {
              // 占位成功 说明从来没买过
              RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
              try {
                boolean acquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                if (acquire) {
                  // 秒杀成功
                  String timeId = IdWorker.getTimeId();
                  // 快速下单,发送MQ消息
                  SecKillOrderTo orderTo = new SecKillOrderTo();
                  orderTo.setOrderSn(timeId);
                  orderTo.setMemberId(responseVo.getId());
                  orderTo.setNum(num);
                  orderTo.setSkuId(redisTo.getSkuId());
                  orderTo.setSecKillPrice(redisTo.getSecKillPrice());
                  orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                  rabbitTemplate.convertAndSend(
                      "order-event-exchange", "order.seckill.order", orderTo);
                  long end = System.currentTimeMillis();
                  log.info("秒杀时间:{}ms", end - start);
                  return timeId;
                } else {
                  return null;
                }
              } catch (InterruptedException e) {
                return null;
              }
            } else {
              // 说明买过了
              return null;
            }
          } else {
            return null;
          }
        } else {
          return null;
        }
      } else {
        return null;
      }
    }
  }
}

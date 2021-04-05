package com.nai.gulimall.seckill.controller;

import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.seckill.service.SecKillService;
import com.nai.gulimall.seckill.to.SecKillSkuRedisTo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class SecKillController {

  @Autowired SecKillService secKillService;

  /**
   * 返回当前时间可以参与的秒杀商品信息
   *
   * @return
   */
  @ResponseBody
  @GetMapping("/currentSecKillSkus")
  public R getCurrentSecKillSkus() {
    log.info("getCurrentSecKillSkus()正在执行");
    List<SecKillSkuRedisTo> list = secKillService.getCurrentSecKillSkus();
    return R.ok().setData(list);
  }

  @ResponseBody
  @GetMapping("/sku/sec/kill/{skuId}")
  public R getSkuSecKillInfo(@PathVariable("skuId") Long skuId) {
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    SecKillSkuRedisTo redisTo = secKillService.getSkuSecKillInfo(skuId);
    return R.ok().setData(redisTo);
  }

  @GetMapping("/kill")
  public String secKill(
      @RequestParam("killId") String killId,
      @RequestParam("key") String key,
      @RequestParam("num") Integer num,
      Model model) {
    String orderSn = secKillService.kill(killId, key, num);
    // 判断是否登录
    model.addAttribute("orderSn", orderSn);
    return "success";
  }
}

package com.nai.gulimall.coupon.controller;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.coupon.entity.CouponHistoryEntity;
import com.nai.gulimall.coupon.service.CouponHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@RestController
@RequestMapping("coupon/couponHistory")
public class CouponHistoryController {
  @Autowired private CouponHistoryService couponHistoryService;

  /** 列表 */
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = couponHistoryService.queryPage(params);

    return R.ok().put("page", page);
  }

  /** 信息 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    CouponHistoryEntity couponHistory = couponHistoryService.getById(id);

    return R.ok().put("couponHistory", couponHistory);
  }

  /** 保存 */
  @RequestMapping("/save")
  public R save(@RequestBody CouponHistoryEntity couponHistory) {
    couponHistoryService.save(couponHistory);

    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  public R update(@RequestBody CouponHistoryEntity couponHistory) {
    couponHistoryService.updateById(couponHistory);

    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    couponHistoryService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }
}

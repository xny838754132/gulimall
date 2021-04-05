package com.nai.gulimall.coupon.controller;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.coupon.entity.CouponEntity;
import com.nai.gulimall.coupon.service.CouponService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券信息
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@RefreshScope // 配置刷新
@RestController
@RequestMapping("coupon/coupon")
public class CouponController {
  @Autowired private CouponService couponService;

  //    @Value("${coupon.user.name}")
  //    private String name;
  //    @Value("${coupon.user.age}")
  //    private Integer age;
  //
  //    @RequestMapping("/test")
  //    public R test(){
  //        return R.ok().put("name",name).put("age",age);
  //    }

  @RequestMapping("/member/list")
  public R memberCoupons() {
    CouponEntity couponEntity = new CouponEntity();
    couponEntity.setCouponName("满100减10");
    return R.ok().put("coupons", Collections.singletonList(couponEntity));
  }

  /** 列表 */
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = couponService.queryPage(params);

    return R.ok().put("page", page);
  }

  /** 信息 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    CouponEntity coupon = couponService.getById(id);

    return R.ok().put("coupon", coupon);
  }

  /** 保存 */
  @RequestMapping("/save")
  public R save(@RequestBody CouponEntity coupon) {
    couponService.save(coupon);

    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  public R update(@RequestBody CouponEntity coupon) {
    couponService.updateById(coupon);

    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    couponService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }
}

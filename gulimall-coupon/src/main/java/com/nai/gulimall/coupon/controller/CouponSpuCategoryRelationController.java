package com.nai.gulimall.coupon.controller;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;
import com.nai.gulimall.coupon.service.CouponSpuCategoryRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 优惠券分类关联
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@RestController
@RequestMapping("coupon/couponSpuCategoryRelation")
public class CouponSpuCategoryRelationController {
  @Autowired private CouponSpuCategoryRelationService couponSpuCategoryRelationService;

  /** 列表 */
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = couponSpuCategoryRelationService.queryPage(params);

    return R.ok().put("page", page);
  }

  /** 信息 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    CouponSpuCategoryRelationEntity couponSpuCategoryRelation =
        couponSpuCategoryRelationService.getById(id);

    return R.ok().put("couponSpuCategoryRelation", couponSpuCategoryRelation);
  }

  /** 保存 */
  @RequestMapping("/save")
  public R save(@RequestBody CouponSpuCategoryRelationEntity couponSpuCategoryRelation) {
    couponSpuCategoryRelationService.save(couponSpuCategoryRelation);

    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  public R update(@RequestBody CouponSpuCategoryRelationEntity couponSpuCategoryRelation) {
    couponSpuCategoryRelationService.updateById(couponSpuCategoryRelation);

    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    couponSpuCategoryRelationService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }
}

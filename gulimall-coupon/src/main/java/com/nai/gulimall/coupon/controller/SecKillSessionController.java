package com.nai.gulimall.coupon.controller;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.coupon.entity.SecKillSessionEntity;
import com.nai.gulimall.coupon.service.SecKillSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@RestController
@RequestMapping("coupon/secKillSession")
public class SecKillSessionController {

  @Autowired private SecKillSessionService secKillSessionService;

  @GetMapping("/getLast3DaySession")
  public R getLast3DaySession() {
    List<SecKillSessionEntity> sessions = secKillSessionService.getLast3DaySession();
    return R.ok().setData(sessions);
  }

  /** 列表 */
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = secKillSessionService.queryPage(params);

    return R.ok().put("page", page);
  }

  /** 信息 */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    SecKillSessionEntity secKillSession = secKillSessionService.getById(id);

    return R.ok().put("secKillSession", secKillSession);
  }

  /** 保存 */
  @RequestMapping("/save")
  public R save(@RequestBody SecKillSessionEntity secKillSession) {
    secKillSessionService.save(secKillSession);

    return R.ok();
  }

  /** 修改 */
  @RequestMapping("/update")
  public R update(@RequestBody SecKillSessionEntity secKillSession) {
    secKillSessionService.updateById(secKillSession);

    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    secKillSessionService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }
}

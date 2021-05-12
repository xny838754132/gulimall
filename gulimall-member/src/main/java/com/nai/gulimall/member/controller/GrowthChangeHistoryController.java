package com.nai.gulimall.member.controller;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.member.entity.GrowthChangeHistoryEntity;
import com.nai.gulimall.member.service.GrowthChangeHistoryService;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 成长值变化历史记录
 *
 * @author TheNai
 * @email TheNai @gmail.com
 * @date 2021 -02-06 22:39:37
 */
@RestController
@RequestMapping("member/growthChangeHistory")
public class GrowthChangeHistoryController {

  @Autowired
  private GrowthChangeHistoryService growthChangeHistoryService;

  /**
   * 列表
   *
   * @param params the params
   * @return the r
   */
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = growthChangeHistoryService.queryPage(params);

    return R.ok().put("page", page);
  }


  /**
   * 信息
   *
   * @param id the id
   * @return the r
   */
  @RequestMapping("/info/{id}")
  public R info(@PathVariable("id") Long id) {
    GrowthChangeHistoryEntity growthChangeHistory = growthChangeHistoryService.getById(id);

    return R.ok().put("growthChangeHistory", growthChangeHistory);
  }

  /**
   * 保存
   *
   * @param growthChangeHistory the growth change history
   * @return the r
   */
  @RequestMapping("/save")
  public R save(@RequestBody GrowthChangeHistoryEntity growthChangeHistory) {
    growthChangeHistoryService.save(growthChangeHistory);

    return R.ok();
  }

  /**
   * 修改
   *
   * @param growthChangeHistory the growth change history
   * @return the r
   */
  @RequestMapping("/update")
  public R update(@RequestBody GrowthChangeHistoryEntity growthChangeHistory) {
    growthChangeHistoryService.updateById(growthChangeHistory);

    return R.ok();
  }

  /**
   * 删除
   *
   * @param ids the ids
   * @return the r
   */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] ids) {
    growthChangeHistoryService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }

}

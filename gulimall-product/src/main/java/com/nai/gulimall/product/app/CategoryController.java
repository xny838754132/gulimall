package com.nai.gulimall.product.app;

import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.entity.CategoryEntity;
import com.nai.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 商品三级分类
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:00:12
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
  @Autowired private CategoryService categoryService;

  /** 查出所有分类以及子分类,以树形结构组装起来 */
  @RequestMapping("/list/tree")
  // @RequiresPermissions("product:category:list")
  public R list() {
    List<CategoryEntity> entities = categoryService.listWithTree();

    return R.ok().put("data", entities);
  }

  /** 信息 */
  @RequestMapping("/info/{catId}")
  public R info(@PathVariable("catId") Long catId) {
    CategoryEntity category = categoryService.getById(catId);

    return R.ok().put("data", category);
  }

  /** 保存 */
  @RequestMapping("/save")
  // @RequiresPermissions("product:category:save")
  public R save(@RequestBody CategoryEntity category) {
    categoryService.save(category);

    return R.ok();
  }

  @RequestMapping("/update/sort")
  // @RequiresPermissions("product:category:update")
  public R updateSort(@RequestBody CategoryEntity[] category) {
    categoryService.updateBatchById(Arrays.asList(category));
    return R.ok();
  }
  /** 修改 */
  @RequestMapping("/update")
  // @RequiresPermissions("product:category:update")
  public R update(@RequestBody CategoryEntity category) {
    categoryService.updateCascade(category);
    return R.ok();
  }

  /** 删除 */
  @RequestMapping("/delete")
  // @RequiresPermissions("product:category:delete")
  public R delete(@RequestBody Long[] catIds) {
    // 1.检查当前删除的菜单是否被别的地方引用
    categoryService.removeMenuByIds(Arrays.asList(catIds));
    return R.ok();
  }
}

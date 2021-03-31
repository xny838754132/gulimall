package com.nai.gulimall.product.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.entity.BrandEntity;
import com.nai.gulimall.product.entity.CategoryBrandRelationEntity;
import com.nai.gulimall.product.service.CategoryBrandRelationService;
import com.nai.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 品牌分类关联
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:00:12
 */
@RestController
@RequestMapping("product/categoryBrandRelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    /**
     * product/categorybrandrelation/brands/list
     * 1.Controller只来处理请求,接受和校验数据
     * 2.service来接受controller传来的数据,进行业务处理
     * 3.controller接受service处理完的数据 封装成页面指定的VO
     */
    @GetMapping("brands/list")
    public R relationBrandList(@RequestParam(value = "catId", required = true) Long catId) {
        List<BrandEntity> brands = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandVo> collect = brands.stream().map(item -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(item.getBrandId());
            brandVo.setBrandName(item.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", collect);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取当前品牌关联得所有分类
     * catalog/list
     */
    @GetMapping("/catalog/list")
    public R CatalogList(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.

                list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));

        return R.ok().put("data", data);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {

        categoryBrandRelationService.saveDetail(categoryBrandRelation);


        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

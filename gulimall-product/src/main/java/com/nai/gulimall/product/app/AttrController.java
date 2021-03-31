package com.nai.gulimall.product.app;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.entity.ProductAttrValueEntity;
import com.nai.gulimall.product.service.AttrService;
import com.nai.gulimall.product.service.ProductAttrValueService;
import com.nai.gulimall.product.vo.AttrRepoVo;
import com.nai.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:00:12
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    /**
     * /product/attr/base/listForSpu/6
     * 查出规格参数
     */
    @GetMapping("/base/listForSpu/{spuId}")
    public R baseListForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities= productAttrValueService.baseListForSpu(spuId);
        return R.ok().put("data",entities);
    }

    @GetMapping("/{attrType}/list/{catalogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,@PathVariable("catalogId") Long catalogId,
                          @PathVariable("attrType") String type){
        PageUtils page=attrService.queryBaseAttrPage(params,catalogId,type);
        return R.ok().put("page",page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRepoVo respVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }
    /**
     * /product/attr/update/6
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}

package com.nai.gulimall.product.app;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.entity.AttrAttrGroupRelationEntity;
import com.nai.gulimall.product.service.AttrAttrGroupRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 属性&属性分组关联
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:00:12
 */
@RestController
@RequestMapping("product/attrAttrGroupRelation")
public class AttrAttrGroupRelationController {
    @Autowired
    private AttrAttrGroupRelationService attrAttrgroupRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrAttrgroupRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		AttrAttrGroupRelationEntity attrAttrGroupRelation = attrAttrgroupRelationService.getById(id);

        return R.ok().put("attrAttrGroupRelation", attrAttrGroupRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrAttrGroupRelationEntity attrAttrGroupRelation){
		attrAttrgroupRelationService.save(attrAttrGroupRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrAttrGroupRelationEntity attrAttrGroupRelation){
		attrAttrgroupRelationService.updateById(attrAttrGroupRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		attrAttrgroupRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

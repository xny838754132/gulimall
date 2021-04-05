package com.nai.gulimall.coupon.controller;

import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.coupon.entity.SecKillSkuNoticeEntity;
import com.nai.gulimall.coupon.service.SecKillSkuNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 秒杀商品通知订阅
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:30:01
 */
@RestController
@RequestMapping("coupon/secKillSkuNotice")
public class SecKillSkuNoticeController {
    @Autowired
    private SecKillSkuNoticeService seckillSkuNoticeService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSkuNoticeService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SecKillSkuNoticeEntity seckillSkuNotice = seckillSkuNoticeService.getById(id);

        return R.ok().put("secKillSkuNotice", seckillSkuNotice);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SecKillSkuNoticeEntity seckillSkuNotice){
		seckillSkuNoticeService.save(seckillSkuNotice);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SecKillSkuNoticeEntity seckillSkuNotice){
		seckillSkuNoticeService.updateById(seckillSkuNotice);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		seckillSkuNoticeService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

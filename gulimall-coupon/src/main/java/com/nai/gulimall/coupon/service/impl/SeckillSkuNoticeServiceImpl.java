package com.nai.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;

import com.nai.gulimall.coupon.dao.SeckillSkuNoticeDao;
import com.nai.gulimall.coupon.entity.SecKillSkuNoticeEntity;
import com.nai.gulimall.coupon.service.SeckillSkuNoticeService;


@Service("seckillSkuNoticeService")
public class SeckillSkuNoticeServiceImpl extends ServiceImpl<SeckillSkuNoticeDao, SecKillSkuNoticeEntity> implements SeckillSkuNoticeService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecKillSkuNoticeEntity> page = this.page(
                new Query<SecKillSkuNoticeEntity>().getPage(params),
                new QueryWrapper<SecKillSkuNoticeEntity>()
        );

        return new PageUtils(page);
    }

}
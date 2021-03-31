package com.nai.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;

import com.nai.gulimall.coupon.dao.SeckillPromotionDao;
import com.nai.gulimall.coupon.entity.SecKillPromotionEntity;
import com.nai.gulimall.coupon.service.SeckillPromotionService;


@Service("seckillPromotionService")
public class SeckillPromotionServiceImpl extends ServiceImpl<SeckillPromotionDao, SecKillPromotionEntity> implements SeckillPromotionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecKillPromotionEntity> page = this.page(
                new Query<SecKillPromotionEntity>().getPage(params),
                new QueryWrapper<SecKillPromotionEntity>()
        );

        return new PageUtils(page);
    }

}
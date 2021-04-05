package com.nai.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.coupon.dao.SecKillPromotionDao;
import com.nai.gulimall.coupon.entity.SecKillPromotionEntity;
import com.nai.gulimall.coupon.service.SecKillPromotionService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("secKillPromotionService")
public class SecKillPromotionServiceImpl extends ServiceImpl<SecKillPromotionDao, SecKillPromotionEntity> implements SecKillPromotionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecKillPromotionEntity> page = this.page(
                new Query<SecKillPromotionEntity>().getPage(params),
                new QueryWrapper<SecKillPromotionEntity>()
        );

        return new PageUtils(page);
    }

}
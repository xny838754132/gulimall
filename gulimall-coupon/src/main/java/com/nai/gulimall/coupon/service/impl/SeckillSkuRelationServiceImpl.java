package com.nai.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;

import com.nai.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.nai.gulimall.coupon.entity.SecKillSkuRelationEntity;
import com.nai.gulimall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SecKillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecKillSkuRelationEntity> page = this.page(
                new Query<SecKillSkuRelationEntity>().getPage(params),
                new QueryWrapper<SecKillSkuRelationEntity>()
        );

        return new PageUtils(page);
    }

}
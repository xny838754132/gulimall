package com.nai.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;

import com.nai.gulimall.coupon.dao.SeckillSessionDao;
import com.nai.gulimall.coupon.entity.SecKillSessionEntity;
import com.nai.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SecKillSessionEntity> implements SeckillSessionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecKillSessionEntity> page = this.page(
                new Query<SecKillSessionEntity>().getPage(params),
                new QueryWrapper<SecKillSessionEntity>()
        );

        return new PageUtils(page);
    }

}
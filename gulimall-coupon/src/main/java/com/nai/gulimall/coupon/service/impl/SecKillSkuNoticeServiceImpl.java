package com.nai.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.coupon.dao.SecKillSkuNoticeDao;
import com.nai.gulimall.coupon.entity.SecKillSkuNoticeEntity;
import com.nai.gulimall.coupon.service.SecKillSkuNoticeService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("secKillSkuNoticeService")
public class SecKillSkuNoticeServiceImpl extends ServiceImpl<SecKillSkuNoticeDao, SecKillSkuNoticeEntity> implements SecKillSkuNoticeService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecKillSkuNoticeEntity> page = this.page(
                new Query<SecKillSkuNoticeEntity>().getPage(params),
                new QueryWrapper<SecKillSkuNoticeEntity>()
        );

        return new PageUtils(page);
    }

}
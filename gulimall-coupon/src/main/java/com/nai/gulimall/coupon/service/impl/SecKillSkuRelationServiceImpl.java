package com.nai.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.coupon.dao.SecKillSkuRelationDao;
import com.nai.gulimall.coupon.entity.SecKillSkuRelationEntity;
import com.nai.gulimall.coupon.service.SecKillSkuRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("secKillSkuRelationService")
public class SecKillSkuRelationServiceImpl
    extends ServiceImpl<SecKillSkuRelationDao, SecKillSkuRelationEntity>
    implements SecKillSkuRelationService {

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    QueryWrapper<SecKillSkuRelationEntity> queryWrapper = new QueryWrapper<>();
    String promotionSessionId = (String) params.get("promotionSessionId");
    if (!StringUtils.isEmpty(promotionSessionId)) {
      // 场次id不是空
      queryWrapper.eq("promotion_session_id", promotionSessionId);
    }
    IPage<SecKillSkuRelationEntity> page =
        this.page(new Query<SecKillSkuRelationEntity>().getPage(params), queryWrapper);

    return new PageUtils(page);
  }
}

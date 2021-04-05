package com.nai.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.coupon.dao.SecKillSessionDao;
import com.nai.gulimall.coupon.entity.SecKillSessionEntity;
import com.nai.gulimall.coupon.entity.SecKillSkuRelationEntity;
import com.nai.gulimall.coupon.service.SecKillSessionService;
import com.nai.gulimall.coupon.service.SecKillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("secKillSessionService")
public class SecKillSessionServiceImpl extends ServiceImpl<SecKillSessionDao, SecKillSessionEntity>
    implements SecKillSessionService {

  @Autowired SecKillSkuRelationService secKillSkuRelationService;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<SecKillSessionEntity> page =
        this.page(
            new Query<SecKillSessionEntity>().getPage(params),
            new QueryWrapper<SecKillSessionEntity>());

    return new PageUtils(page);
  }

  @Override
  public List<SecKillSessionEntity> getLast3DaySession() {
    // 计算最近三天
    List<SecKillSessionEntity> list =
        this.list(
            new QueryWrapper<SecKillSessionEntity>().between("start_time", startTime(), endTime()));
    if (!CollectionUtils.isEmpty(list)) {
      return list.stream()
          .peek(
              session -> {
                List<SecKillSkuRelationEntity> secKillSkuRelationEntities =
                    secKillSkuRelationService.list(
                        new QueryWrapper<SecKillSkuRelationEntity>()
                            .eq("promotion_session_id", session.getId()));
                session.setRelationSkus(secKillSkuRelationEntities);
              })
          .collect(Collectors.toList());
    }
    return null;
  }

  public String startTime() {
    return LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        .format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss"));
  }

  public String endTime() {
    return LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX)
        .format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss"));
  }
}

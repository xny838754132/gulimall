package com.nai.gulimall.product.feign.fallback;

import com.nai.gulimall.common.exception.BizCodeEnum;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.product.feign.SecKillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecKillFeignFallBack implements SecKillFeignService {

  @Override
  public R getSkuSecKillInfo(Long skuId) {
    log.info("熔断方法调用..getSkuSecKillInfo");
    return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMessage());
  }
}

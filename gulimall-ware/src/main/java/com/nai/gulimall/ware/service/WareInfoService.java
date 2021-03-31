package com.nai.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.ware.entity.WareInfoEntity;
import com.nai.gulimall.ware.vo.FareResponseVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:58:53
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据收获地址计算运费
     * @param addrId
     * @return
     */
    FareResponseVo getFate(Long addrId);
}


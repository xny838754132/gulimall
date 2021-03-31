package com.nai.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.nai.gulimall.product.entity.AttrAttrGroupRelationEntity;
import com.nai.gulimall.product.service.AttrAttrGroupRelationService;
import com.nai.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author 83875
 */
@Service("attrAttrGroupRelationService")
public class AttrAttrGroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrGroupRelationEntity> implements AttrAttrGroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrGroupRelationEntity> page = this.page(
                new Query<AttrAttrGroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrGroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatch(List<AttrGroupRelationVo> vos) {
        List<AttrAttrGroupRelationEntity> collect = vos.stream().map(item -> {
            AttrAttrGroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrGroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }
}
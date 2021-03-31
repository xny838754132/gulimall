package com.nai.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.constant.ProductConstant;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.nai.gulimall.product.dao.AttrDao;
import com.nai.gulimall.product.dao.AttrGroupDao;
import com.nai.gulimall.product.dao.CategoryDao;
import com.nai.gulimall.product.entity.AttrAttrGroupRelationEntity;
import com.nai.gulimall.product.entity.AttrEntity;
import com.nai.gulimall.product.entity.AttrGroupEntity;
import com.nai.gulimall.product.entity.CategoryEntity;
import com.nai.gulimall.product.service.AttrService;
import com.nai.gulimall.product.service.CategoryService;
import com.nai.gulimall.product.vo.AttrGroupRelationVo;
import com.nai.gulimall.product.vo.AttrRepoVo;
import com.nai.gulimall.product.vo.AttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //1.保存基本数据
        this.save(attrEntity);
        //2.保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrGroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrGroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catalogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().
                eq("attr_type", "base".equalsIgnoreCase(type) ?
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catalogId != 0) {
            queryWrapper.eq("catalog_id", catalogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRepoVo> respVos = records.stream().map((attrEntity) -> {
            AttrRepoVo attrRespVo = new AttrRepoVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)) {
                QueryWrapper<AttrAttrGroupRelationEntity> wrapper = new QueryWrapper<>();
                if (attrEntity.getAttrId() != null) {
                    wrapper.eq("attr_id", attrEntity.getAttrId());
                    AttrAttrGroupRelationEntity attrId = attrAttrgroupRelationDao.selectOne(wrapper);
                    if (attrId != null && attrId.getAttrGroupId() != null) {
                        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                        if (attrGroupEntity != null) {
                            attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                        }
                    }
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatalogId());
            if (categoryEntity != null) {
                attrRespVo.setCatalogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'attrInfo:'+#root.args[0]")
    @Override
    public AttrRepoVo getAttrInfo(Long attrId) {
        AttrRepoVo respVo = new AttrRepoVo();
        AttrEntity attrEntity = this.getById(attrId);

        BeanUtils.copyProperties(attrEntity, respVo);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //设置分组信息
            AttrAttrGroupRelationEntity attrGroupRelation = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrGroupRelationEntity>().eq("attr_id", attrId));
            if (attrGroupRelation != null) {
                respVo.setAttrGroupId(attrGroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupRelation.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        //设置分类信息
        Long catalogId = attrEntity.getCatalogId();
        Long[] catalogPath = categoryService.findCatalogPath(catalogId);
        respVo.setCatalogPath(catalogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catalogId);
        if (categoryEntity != null) {
            respVo.setCatalogName(categoryEntity.getName());
        }
        return respVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrGroupRelationEntity relationEntity = new AttrAttrGroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrGroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                //1.修改分组关联
                attrAttrgroupRelationDao.update(relationEntity, new UpdateWrapper<AttrAttrGroupRelationEntity>()
                        .eq("attr_id", attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 根据分组id查找关联的所有属性
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrGroupRelationEntity> entities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrGroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIds = entities.stream().map(AttrAttrGroupRelationEntity::getAttrId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attrIds)) {
            return null;
        }
        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);
        return (List<AttrEntity>) attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrGroupRelationEntity> entities = Arrays.stream(vos).map(item -> {
            AttrAttrGroupRelationEntity relationEntity = new AttrAttrGroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());

        attrAttrgroupRelationDao.deleteBatchRelation(entities);
    }

    /**
     * 获取当前分组没有关联的所有属性
     *
     * @param attrGroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Long attrGroupId, Map<String, Object> params) {
        //1.当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catalogId = attrGroupEntity.getCatalogId();
        //2.当前分组只能关联别的分组没有引用的属性
        //找到当前分类 下的其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catalog_id", catalogId));
        List<Long> collect = group.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        //找到这些分组关联的属性
        List<AttrAttrGroupRelationEntity> groupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrGroupRelationEntity>()
                .in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(AttrAttrGroupRelationEntity::getAttrId).collect(Collectors.toList());
        //从属性表中移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catalog_id", catalogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            queryWrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {

        return baseMapper.selectSearchAttrIds(attrIds);
    }
}
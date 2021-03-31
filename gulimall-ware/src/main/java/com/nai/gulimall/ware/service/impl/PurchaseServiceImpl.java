package com.nai.gulimall.ware.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.constant.WareConstant;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.ware.dao.PurchaseDao;
import com.nai.gulimall.ware.entity.PurchaseDetailEntity;
import com.nai.gulimall.ware.entity.PurchaseEntity;
import com.nai.gulimall.ware.service.PurchaseDetailService;
import com.nai.gulimall.ware.service.PurchaseService;
import com.nai.gulimall.ware.service.WareSkuService;
import com.nai.gulimall.ware.vo.MergeVo;
import com.nai.gulimall.ware.vo.PurchaseDoneVo;
import com.nai.gulimall.ware.vo.PurchaseItemDoneVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author 83875
 */
@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService detailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            //新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        PurchaseEntity purchase = this.getById(purchaseId);
        if (purchase != null) {
            if (purchase.getStatus() == WareConstant.PurchaseEnum.ASSIGNED.getCode() ||
                    purchase.getStatus() == WareConstant.PurchaseEnum.CREATED.getCode()) {
                List<PurchaseDetailEntity> collect = items.stream().map(i -> {
                    PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                    detailEntity.setId(i);
                    detailEntity.setPurchaseId(finalPurchaseId);
                    detailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
                    return detailEntity;
                }).collect(Collectors.toList());
                detailService.updateBatchById(collect);
                PurchaseEntity purchaseEntity = new PurchaseEntity();
                purchaseEntity.setId(purchaseId);
                purchaseEntity.setUpdateTime(new Date());
                this.updateById(purchaseEntity);
            }
        }

    }

    @Override
    public void received(List<Long> ids) {
        //1.确认当前采购单是新建或者是已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).peek(item -> {
            item.setStatus(WareConstant.PurchaseEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
        }).collect(Collectors.toList());
        //2.改变采购单的状态
        if (!CollectionUtils.isEmpty(collect)) {
            this.updateBatchById(collect);
        }
        //3.改变采购单采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> list = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities = list.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.BUY.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(detailEntities)) {
                detailService.updateBatchById(detailEntities);
            }
        });
    }

    @Override
    public void done(PurchaseDoneVo doneVo) {

        Long id = doneVo.getId();

        //2.改变采购项状态
        boolean flag=true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates=new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus()==WareConstant.PurchaseDetailEnum.HAVE_ERROR.getCode()){
                flag=false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailEnum.FINISH.getCode());
                //3.将成功采购的进行入库
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        detailService.updateBatchById(updates);
        //1.改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseEnum.FINISH.getCode():WareConstant.PurchaseEnum.HAVE_ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
        //3.将成功采购的进行入库

    }
}
package com.nai.gulimall.ware.service.impl;


import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.exception.NoStockException;
import com.nai.gulimall.common.to.mq.OrderTo;
import com.nai.gulimall.common.to.mq.StockDetailTo;
import com.nai.gulimall.common.to.mq.StockLockedTo;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.common.utils.R;
import com.nai.gulimall.ware.dao.WareSkuDao;
import com.nai.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.nai.gulimall.ware.entity.WareOrderTaskEntity;
import com.nai.gulimall.ware.entity.WareSkuEntity;
import com.nai.gulimall.ware.feign.OrderFeignService;
import com.nai.gulimall.ware.feign.ProductFeignService;
import com.nai.gulimall.ware.service.WareOrderTaskDetailService;
import com.nai.gulimall.ware.service.WareOrderTaskService;
import com.nai.gulimall.ware.service.WareSkuService;
import com.nai.gulimall.ware.vo.OrderItemVo;
import com.nai.gulimall.ware.vo.OrderVo;
import com.nai.gulimall.ware.vo.SkuHasStockVo;
import com.nai.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public void unLockStock(StockLockedTo to) {
        //库存工作单的id
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity detailEntity = orderTaskDetailService.getById(detailId);
        //解锁
        //1.查询数据库关于这个订单的锁库存信息
        //有:证明库存锁定成功了
        //解锁:订单情况 1.没有这个订单,必须解锁
        // 2.有订单 不是解锁库存,订单状态:已取消,解锁库存,没取消:不能解锁库存
        //没有,库存锁定失败了,库存回滚了,这种情况无需解锁
        if (detailEntity != null) {
            //解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            //根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //订单不存在或者订单已经被取消了,才能解锁库存
                    if (taskEntity.getTaskStatus() == 1) {
                        //当前库存工作单详情,状态1 已锁定但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                throw new RuntimeException("远程服务失败");
            }
            //消息拒绝以后重新放到队列里面,让别人继续进行消费解锁
        }
        //无需解锁
    }

    /**
     * 防止订单服务卡顿,导致订单状态消息一直改不了,库存消息优先到期,查订单状态为新建状态,什么都不做就走了
     * 导致卡顿订单永远不能解锁库存
     *
     * @param orderTo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新的库存解锁状态,防止重复解锁
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = taskEntity.getId();
        //按照库存工作单找到所有没有解锁的库存 进行解锁
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getTaskId());
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskId) {
        baseMapper.unLockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskId);
        entity.setLockStatus(2);
        orderTaskDetailService.updateById(entity);

    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("sku_id", key);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1.判断没有库存记录-新增
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(new QueryWrapper<WareSkuEntity>()
                .eq("sku_id", skuId).eq("ware_id", wareId));
        if (org.springframework.util.CollectionUtils.isEmpty(wareSkuEntities)) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                //远程查询sku的名字.如果失败,整个事物 无需回滚
                //1.自己catch异常
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception ignored) {

            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return skuIds.stream().map(sku -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            //查询当前sku的总库存量
            Long count = baseMapper.getSkuStock(sku);
            skuHasStockVo.setSkuId(sku);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    /**
     * 为某个订单锁定库存
     * 只要是运行时异常都会回滚
     * <p>
     * 库存解锁的场景
     * 1).下订单成功,订单获取没有支付被系统自动取消\被用户手动取消.都要解锁库存
     * 2).下订单成功,库存锁定成功,但是接下来的业务调用失败,导致订单回滚,之前锁定的库存就要解锁
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /*
        保存库存工作单的详情
        追溯.
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        taskEntity.setTaskStatus(1);
        orderTaskService.save(taskEntity);
        //1.按照下单的收货地址,找到一个就近仓库,锁定库存

        //1.赵高每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = baseMapper.listWareHasSkuStock(skuId);
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());
        Boolean allLock = true;
        //2.锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if (CollectionUtils.isEmpty(wareIds)) {
                throw new NoStockException(skuId);
            }
            //如果每一个商品都所成功,将当前商品锁定了几件的工作单详情记录发给MQ
            //如果锁定失败.前面保存的工作单信息就回滚了.发送出去的消息即使要解锁记录,由于去数据库查不到指定id,所以就不用解锁了

            for (Long wareId : wareIds) {
                //成功返回1,否则就是0
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    //锁成功
                    skuStocked = true;
                    //TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    //只发id不行,回滚后会找不到数据 防止回滚以后找不到数据
                    lockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                }
                //当前仓库锁失败,重试下一个仓库
            }
            if (!skuStocked) {
                throw new NoStockException(skuId);
            }
        }
        //3.肯定全部都是锁定成功得

        return true;

    }

    @Data
    static
    class SkuWareHasStock {
        private Long skuId;
        private List<Long> wareIds;
        private Integer num;
    }
}
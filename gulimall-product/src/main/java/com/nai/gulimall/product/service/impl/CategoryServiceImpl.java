package com.nai.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.product.dao.CategoryDao;
import com.nai.gulimall.product.entity.CategoryEntity;
import com.nai.gulimall.product.service.CategoryBrandRelationService;
import com.nai.gulimall.product.service.CategoryService;
import com.nai.gulimall.product.vo.Catalog2Vo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity>
    implements CategoryService {

  @Autowired CategoryBrandRelationService categoryBrandRelationService;

  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired RedissonClient redisson;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<CategoryEntity> page =
        this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

    return new PageUtils(page);
  }

  @Override
  public List<CategoryEntity> listWithTree() {
    // 1.查出所有分类
    List<CategoryEntity> entities = baseMapper.selectList(null);
    // 2.组装成父子树形结构

    // 2.1找到所有的一级分类

    return entities.stream()
        .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
        .peek((menu) -> menu.setChildren(getChildren(menu, entities)))
        .sorted(
            (menu1, menu2) -> {
              return (menu1.getSort() == null ? 0 : menu1.getSort())
                  - (menu2.getSort() == null ? 0 : menu2.getSort());
            })
        .collect(Collectors.toList());
  }

  /** 递归查找所有菜单的子菜单 */
  private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
    return all.stream()
        .filter(
            categoryEntity -> {
              return categoryEntity.getParentCid().equals(root.getCatId());
            })
        .peek(
            categoryEntity -> {
              // 1、找到子菜单
              categoryEntity.setChildren(getChildren(categoryEntity, all));
            })
        .sorted(
            (menu1, menu2) -> {
              // 2、菜单的排序
              return (menu1.getSort() == null ? 0 : menu1.getSort())
                  - (menu2.getSort() == null ? 0 : menu2.getSort());
            })
        .collect(Collectors.toList());
  }

  @Override
  public void removeMenuByIds(List<Long> asList) {
    // TODO 检查当前删除菜单是否被别的地方引用
    // 逻辑删除
    baseMapper.deleteBatchIds(asList);
  }

  @Override
  public Long[] findCatalogPath(Long catalogId) {
    List<Long> paths = new ArrayList<>();
    List<Long> parentPath = findParentPath(catalogId, paths);
    Collections.reverse(parentPath);
    return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
  }

  private List<Long> findParentPath(Long catalogId, List<Long> paths) {
    // 1.收集当前节点id
    paths.add(catalogId);
    CategoryEntity byId = this.getById(catalogId);
    if (byId.getParentCid() != 0) {
      findParentPath(byId.getParentCid(), paths);
    }
    return paths;
  }

  /**
   * 级联更新所有的关联数据
   *
   * @param category @CacheEvict 失效模式 1.同时进行多种缓存操作 @Caching @Caching(evict = { @CacheEvict(value =
   *     {"category"}, key = "'getLevel1Categories'"), @CacheEvict(value = "category",key =
   *     "'getCatalogJson'") }) 2.指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
   *     3.存储同一类型的数据,我们都可以指定成同一个分区.约定-->分区名为默认的前缀 @CacheEvict(value = "category",allEntries =
   *     true)//失效模式 @CachePut //双写模式
   */
  @CacheEvict(value = "category", allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateCascade(CategoryEntity category) {
    this.updateById(category);
    categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    // 同时修改缓存中的菜单
    // redis.del("catalogJson);等待下一次主动查询去更新缓存
  }

  /**
   * 1.每一个需要缓存的数据,都来指定要放到哪个名字的缓存.[缓存的分区(按照业务类型分)] 2.@Cacheable({"category"})
   * 代表当前方法的结果需要缓存,如果缓存中有,方法就不会调用. 如果缓存中没有,就会调用方法,最后将方法的结果放入缓存 3.默认行为 1).如果缓存中没有,就会调用方法
   * 2).key是默认自动生成的:缓存的名字::SimpleKey [] (自主生成的key值) 3).缓存的value的值.默认使用jdk序列化机制,将序列化后的数据存到redis
   * 4).默认ttl的时间 -1; 4.自定义操作: 1).指定生成的缓存使用的key: key属性指定->接收一个SpEL表达式
   * https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-spel-context
   * 2).指定缓存的数据的存活时间:配置文件中修改配置 3).将数据保存为JSON格式: 4.Spring-Cache的不足: 1).读模式: 缓存穿透:查询一个null数据. 解决:缓存空数据
   * spring.cache.redis.cache-null-values=true 缓存击穿:大量并发进来同时查询一个正好过期的数据. 解决: 加锁; 默认是无加锁的 sync =
   * true(加锁,解决击穿) 缓存雪崩:大量的key同时过期.解决:加随机时间.加上过期时间 spring.cache.redis.time-to-live=3600000
   * 2).写模式:(缓存与数据库一致) (1).读写加锁 (2).引入Canal,感知到MySQL的更新去更新redis (3).读多写多,直接去数据库查询就好
   *
   * <p>总结: 常规数据(读多写少,即时性,一致性要求不高的数据),完全可以使用Spring-Cache;写模式(只要缓存的数据有过期时间即可)
   *
   * <p>特殊数据:特殊设计
   *
   * <p>原理: CacheManager-> Cache ->Cache(RedisCache)负责缓存的读写->Cache负责缓存的读写
   */
  @Cacheable(
      value = {"category"},
      key = "#root.method.name",
      sync = true)
  @Override
  public List<CategoryEntity> getLevel1Categories() {
    System.out.println("getLevel1Categories...");

    return baseMapper.selectList(
        new QueryWrapper<CategoryEntity>().eq("parent_cid", 0).orderByAsc("sort"));
  }

  @Cacheable(value = "category", key = "#root.methodName")
  @Override
  public Map<Long, List<Catalog2Vo>> getCatalogJson() {
    List<CategoryEntity> selectList = baseMapper.selectList(null);
    List<CategoryEntity> level1 = getParentCid(selectList, 0L);
    return level1.stream()
        .collect(
            Collectors.toMap(
                CategoryEntity::getCatId,
                v -> {
                  // 每一个一级分类,查到这个一级分类的耳机分类
                  List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
                  List<Catalog2Vo> catalog2Vos = new ArrayList<>();
                  if (categoryEntities != null) {
                    catalog2Vos =
                        categoryEntities.stream()
                            .map(
                                l2 -> {
                                  // 找当前2级分类的三级分类
                                  List<CategoryEntity> level3Catalog =
                                      getParentCid(selectList, l2.getCatId());
                                  List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                                  if (level3Catalog != null) {
                                    catalog3Vos =
                                        level3Catalog.stream()
                                            .map(
                                                l3 -> {
                                                  return new Catalog2Vo.Catalog3Vo(
                                                      l2.getCatId().toString(),
                                                      l3.getCatId().toString(),
                                                      l3.getName());
                                                })
                                            .collect(Collectors.toList());
                                  }
                                  // 封装成指定格式
                                  return new Catalog2Vo(
                                      v.getCatId().toString(),
                                      catalog3Vos,
                                      l2.getCatId().toString(),
                                      l2.getName());
                                })
                            .collect(Collectors.toList());
                  }
                  return catalog2Vos;
                }));
  }

  public Map<Long, List<Catalog2Vo>> getCatalogJson2() {
    // 产生堆外内存溢出
    /*
        1.空结果缓存:解决缓存穿透
        2.设置过期时间(姐随机值):解决缓存雪崩
        3.加锁:解决缓存击穿
    */
    // 1.假如缓存逻辑,缓存中寸的数据是json字符串
    // JSON跨语言跨平台兼容
    String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
    if (StringUtils.isEmpty(catalogJson)) {
      // 2.缓存中没有,查询数据库
      System.out.println("缓存不命中,查询数据库");
      // 3.将查到的数据放入缓存,将对象转为JSON放入缓存中
      return getCatalogJsonFromDbWithRedissonLock();
    }
    System.out.println("缓存命中,直接返回");
    // 将拿到的JSON字符串逆转为能用的对象类型(序列化与反序列化)
    return JSON.parseObject(catalogJson, new TypeReference<Map<Long, List<Catalog2Vo>>>() {});
    // 1).springBoot2.0 以后默认使用lettuce作为操作redis的客户端,它使用netty进行网络通信
    // 2).lettuce的BUG导致netty堆外内存溢出 -Xmx300m:netty如果没有指定堆外内存,默认使用-Xmx300m 作为堆外内存
    //  可以通过-Dio.netty.maxDirectMemory 进行设置
    // 解决方案 :不能使用-Dio.netty.maxDirectMemory 只去调大堆外内存.
    // 1).升级lettuce客户端  2).切换使用jedis
    // redisTemplate:
    // lettuce,jedis 操作redis 底层客户端. 再次封装成redisTemplate;
  }

  /**
   * 缓存中的数据如何和数据库的数据保持一致 缓存一致性 1).双写模式 2).失效模式
   *
   * @return
   */
  public Map<Long, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
    // 1.锁的名字,锁的粒度,越洗细越快
    // 锁的粒度: 具体缓存得是某个数据,11号商品; product-11-lock product-12-lock product-lock
    RLock lock = redisson.getLock("catalogJson-lock");
    lock.lock();
    Map<Long, List<Catalog2Vo>> dataFromDb;
    try {
      dataFromDb = getDataFromDb();
    } finally {
      lock.unlock();
    }
    return dataFromDb;
  }

  /**
   * 从数据库查询并封装分类数据
   *
   * @return
   */
  public Map<Long, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {
    /*
       只要同一把锁,就能锁住需要这个锁的所有线程
       1.synchronized (this):SpringBoot所有的组件在容器中都是单例的.
        本地所:synchronized JUC(lock),在分布式情况下,想要锁住锁所有,必须要使用分布式锁
    */
    synchronized (this) {
      // 得到锁以后,我们应该再去缓存中确定一次,如果没有才需要再次查询
      return getDataFromDb();
    }
  }

  public Map<Long, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
    // 1.占分布式锁,去redis占坑
    // 2.设置国过期时间,必须和加锁是同步的,原子的
    String uuid = UUID.randomUUID().toString();
    Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
    if (lock) {
      System.out.println("获取分布式锁成功~");
      // 加锁成功..执行业务
      Map<Long, List<Catalog2Vo>> dataFromDb;
      try {
        dataFromDb = getDataFromDb();
      } finally {
        String script =
            "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        // 删除锁(原子删锁)
        Long lock1 =
            redisTemplate.execute(
                new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
      }
      // 获取值对比+对比成功删除=原子操作
      // 删除锁
      // 拿到值后,锁过期,别人又占了一个锁,此时redis锁的value已经更新成别的人的value了,就会删除别人的锁
      return dataFromDb;
    } else {
      // 加锁失败...重试 synchronized ()
      // 自旋的方式
      // 休眠100ms
      System.out.println("获取分布式锁失败~重试");
      try {
        Thread.sleep(200);
      } catch (Exception e) {

      }
      return getCatalogJsonFromDbWithRedisLock();
    }
  }

  private Map<Long, List<Catalog2Vo>> getDataFromDb() {
    // 得到锁以后,我们应该再去缓存中确定一次,如果没有才需要再次查询
    String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
    if (StringUtils.isNotEmpty(catalogJson)) {
      // 缓存不为空,直接返回
      return JSON.parseObject(catalogJson, new TypeReference<Map<Long, List<Catalog2Vo>>>() {});
    }
    System.out.println("查询了数据库");

    //           1.优化:将数据库的多次查询变为一次

    List<CategoryEntity> selectList = baseMapper.selectList(null);
    // 1.查出所有分类
    List<CategoryEntity> level1 = getParentCid(selectList, 0L);
    // 2.封装数据
    Map<Long, List<Catalog2Vo>> map =
        level1.stream()
            .collect(
                Collectors.toMap(
                    CategoryEntity::getCatId,
                    v -> {
                      // 每一个一级分类,查到这个一级分类的耳机分类
                      List<CategoryEntity> categoryEntities =
                          getParentCid(selectList, v.getCatId());
                      List<Catalog2Vo> catalog2Vos = new ArrayList<>();
                      if (categoryEntities != null) {
                        catalog2Vos =
                            categoryEntities.stream()
                                .map(
                                    l2 -> {
                                      // 找当前2级分类的三级分类
                                      List<CategoryEntity> level3Catalog =
                                          getParentCid(selectList, l2.getCatId());
                                      List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                                      if (level3Catalog != null) {
                                        catalog3Vos =
                                            level3Catalog.stream()
                                                .map(
                                                    l3 -> {
                                                      return new Catalog2Vo.Catalog3Vo(
                                                          l2.getCatId().toString(),
                                                          l3.getCatId().toString(),
                                                          l3.getName());
                                                    })
                                                .collect(Collectors.toList());
                                      }
                                      // 封装成指定格式
                                      return new Catalog2Vo(
                                          v.getCatId().toString(),
                                          catalog3Vos,
                                          l2.getCatId().toString(),
                                          l2.getName());
                                    })
                                .collect(Collectors.toList());
                      }
                      return catalog2Vos;
                    }));
    String json = JSON.toJSONString(map);
    redisTemplate.opsForValue().set("catalogJSON", json, 1, TimeUnit.DAYS);
    return map;
  }

  private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryList, Long parentCid) {
    return categoryList.stream()
        .filter(item -> item.getParentCid().equals(parentCid))
        .collect(Collectors.toList());
  }
}

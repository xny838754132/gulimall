package com.nai.gulimall.product.web;

import com.nai.gulimall.product.entity.CategoryEntity;
import com.nai.gulimall.product.service.CategoryService;
import com.nai.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 查出所有的1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();
        //视图解析器进行拼串
        model.addAttribute("categories", categoryEntities);
        //classpath:/templates/+返回值+.html
        return "index";
    }

    @GetMapping("/index/json/catalog.json")
    @ResponseBody
    public Map<Long, List<Catalog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1.获取一把锁,只要锁名一样就是同一把锁
        RLock myLock = redisson.getLock("myLock");
        //2.加锁
        //阻塞式等待 默认加的锁都是30S时间
        //10S自动解锁  自动解锁时间一定要大于业务的执行时间
        //1).锁的自动续期,如果业务超长,运行期间会自动给锁续上新的30S,不用担心业务时间长锁自动过期被删掉.
        //2).加锁的业务只要运行完成就不会给当前所续期,即使不手动解锁,锁也会默认在30S以后自动删除
        myLock.lock();
        //问题myLock.lock(10, TimeUnit.SECONDS); 锁时间到了以后,不会自动续期
        //1.如果我们传递了锁的超时时间,就发送给redis执行脚本,进行占锁,默认超时时间就是我们指定的时间
        //2.如果我们未指定锁的超时时间,就使用lockWatchdogTimeout = 30 * 1000;
        //只要占锁成功,就会启动一个定时任务[重新给锁设置过期时间,新的过期时间就是看门狗的默认时间lockWatchdogTimeout = 30 * 1000],每隔10S都会自定续期到30S
        //internalLockLeaseTime[看门狗时间]/3, 10S后续期
        //最佳实战
        //1).myLock.lock(30, TimeUnit.SECONDS); 省掉了续期的操作.手动解锁.
        try {
            System.out.println("加锁成功,执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            //3.解锁 假设解锁代码没有运行,redisson会不会出现死锁
            System.out.println("释放锁" + Thread.currentThread().getId());
            myLock.unlock();
        }
        return "hello";
    }

    /**
     * 保证一定能读到最新数据,修改期间,写锁是一个排他锁(互斥锁,独享锁).读锁是一个共享锁
     * 写锁没释放,读锁就必须等待
     * 读+读:相当于无锁,并发度,只会再redis中记录好,所有的读锁,他们会同时加锁成功
     * 写+读:等待写锁释放
     * 写+写:阻塞方式
     * 读+写:有读锁,写也需要等待
     * 只要有写的存在,都必须等待
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        String s = "";
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        s = UUID.randomUUID().toString();
        RLock rLock = lock.writeLock();
        try {
            rLock.lock();
            System.out.println("写锁加锁成功..." + Thread.currentThread().getId());
            //1.改数据加写锁,读数据加读锁
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放" + Thread.currentThread().getId());
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        //加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        System.out.println("读锁加锁成功..." + Thread.currentThread().getId());
        try {
            Thread.sleep(30000);
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放" + Thread.currentThread().getId());
        }
        return s;
    }

    /**
     * 车库停车
     * 3车位
     * 信号量也可以用做分布式限流
     *
     * @return
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //获取一个信号,获取一个值,拿一个占一个车位
        // park.acquire();阻塞式获取
        boolean b = park.tryAcquire();
        //park.tryAcquire();尝试获取信号量
        if (b) {
            //执行业务
        } else {
            return "当前流量过大";
        }
        return "ok" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //释放一个车位
//        Semaphore semaphore=new Semaphore(5);JUC
//        semaphore.release();
//        semaphore.acquire(); JUC
        park.release();
        return "ok";
    }

    /**
     * 放假,锁门
     * 1班没人了,2班没人了
     * 5个班全部走完,我们可以锁大门
     */
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        //等待闭锁都完成
        door.await();
        return "放假了...";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        //计数-1
        door.countDown();
//        CountDownLatch
        return id + "班的人都走了...";
    }


}

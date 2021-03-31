package com.nai.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 整合MybatisPlus
 * 1、导入依赖
 * 2、配置
 *  1）、配置数据源
 *      (1)、配置数据库驱动
 *      (2)、在application.yml中配置数据源
 *  2)、配置mybatis-plus
 *      (1)、使用@MapperScan
 *      (2)、告诉mybatis-plus，SQL映射文件位置
 *
 * 2.逻辑删除
 *   1).配置全局逻辑删除规则
 *   2).加上逻辑删除注解
 *
 *
 * 3.后端校验 JSR303
 *    1).给Bean添校验注解 :package javax.validation.constraints;
 *    2).开启校验功能注解@Valid
 *    校验:校验错误以后会有默认的相应;
 *    3).给校验的bean紧跟一个BindResult就可以获得校验的结果
 *    4).分组校验(多场景的复杂校验)
 *      (1).@NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class})
 *      给校验注解上标注什么情况需要进行校验
 *      (2).@Validated(value = {AddGroup.class})
 *      (3).默认没有使用指定分组的校验注解,在分组校验的情况下不生效,只会在@Validated下生效
 *    5).自定义校验
 *      (1).编写一个自定义的校验注解
 *      (2).编写一个自定义的校验器
 *      (3).关联自定义校验器和自定义注解
 *          @Documented
 *          @Constraint(validatedBy = {ListValueConstraintValidator.class}) 可以指定多个不同的校验器,适配不同类型的校验
 *          @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
 *          @Retention(RUNTIME)
 * 4.统一的异常处理
 * @ControllerAdvice
 *  1).编写异常处理类,使用@ControllerAdvice
 *  2).使用@ExceptionHandle标识方法可以处理异常
 *
 * 5.模板引擎
 *  1).thymeleaf-starter:关闭缓存
 *  2).静态资源都放在static文件夹下,就可以按照路劲直接访问
 *  3).页面放在templates下,可以直接访问
 *      springboot.放问项目的时候,默认会找index
 *  4).页面修改不重启服务器实时更新
 *      1.引入dev-tools
 *      2.修改完页面 使用ctrl+shift+f9 重新编译当前页面 如果是代码配置,推荐重启
 *
 * 6.整合redis
 *  1).引入data-redis-starter
 *  2).简单配置redis的host信息
 *  3).使用springboot自动配置好的StringRedisTemplate来操作redis
 *          redis-Map
 *
 * 7.整合redission作为分布式锁等功能的框架
 *      1).引入依赖
 *      <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.15.0</version>
 *         </dependency>
 *      2).配置
 * 8.整合SpringCache简化缓存开发
 *      1).引入依赖
 *      spring-boot-starter-cache spring-boot-starter-data-redis
 *      2).写配置
 *          (1).自动配置了那些
 *           CacheAutoConfiguration会导入RedisCacheConfiguration
 *           自动配置好了缓存管理器RedisCacheManager
 *          (2).配置redis 作为缓存
 *          (3).测试使用缓存
 *          @Cacheable: Triggers cache population.:触发将数据保存到缓存的操作
 *          @CacheEvict: Triggers cache eviction.:触发将数据从缓存删除的操作
 *          @CachePut: Updates the cache without interfering with the method execution.:不影响方法执行更新缓存
 *          @Caching: Regroups multiple cache operations to be applied on a method.:组合以上多个操作
 *          @CacheConfig: Shares some common cache-related settings at class-level.:在类级别共享缓存的相同配置
 *              1).开启缓存功能 @EnableCaching
 *              2).使用注解完成缓存操作
 *
 *          (4).原理
 *          CacheAutoConfiguration  ->  RedisCacheConfiguration ->
 *          自动配置了RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定使用什么配置
 *          -> 如果RedisCacheConfiguration有就用已有的,如果没有就用默认配置,
 *          -> 想改缓存的配置,只需要给容器中放一个RedisCacheConfiguration即可
 *          -> 就会应用到当前RedisCacheManager管理的所有
 *
 *
 */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.nai.gulimall.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.nai.gulimall.product.dao")
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}

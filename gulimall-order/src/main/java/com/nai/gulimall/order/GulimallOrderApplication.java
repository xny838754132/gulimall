package com.nai.gulimall.order;


import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1.引入AMQP场景:RabbitAutoConfiguration自动生效
 * 2.给容器中自动配置了
 *      RabbitTemplate AmqpAdmin CachingConnectionFactory RabbitMessagingTemplate
 *      所有的属性都是
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 *      public class RabbitProperties
 *      来的
 *
 * 3.给配置文件中配置 spring.rabbitmq 信息
 * 4.@EnableRabbit: @EnableXxx 开启功能
 * 5.监听消息：使用@RabbitListener；必须开启@EnableRabbit
 *  @RabbitListener:类+方法上(监听哪些队列即可)
 *  @RabbitHandler:标在方法上(重载区分不同的消息)
 *
 *
 * 本地事务失效问题:
 *  同一个对象内 事务方法互调默认失效,原因,绕过了代理对象
 *  解决:使用代理对象来调用事务方法
 *   1).引入aop-starter;spring-boot-starter-aop:引入了aspectj
 *   2).开启@EnableAspectJAutoProxy:开启aspectj动态代理,以后所有的动态的代理都是aspectj创建的(即使没有接口也可以创建动态代理)
 *          exposeProxy = true:对外暴露代理对象
 *   3).用代理对象本类互调:
 *          AopContext.currentProxy();
 *
 * Seata控制分布式事务:
 *   1).每一个微服务都必须创建undo_log;
 *   2).安装事务协调器: https://github.com/seata/seata/releases
 *   3).整合
 *          1.导入依赖 spring-cloud-starter-alibaba-seata seata-spring-boot-starter
 *          2.解压并启动seata-server
 *              registry.conf 注册中心相关配置;修改registry type=nacos file.conf
 *          3.所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 *          4.每个微服务都必须导入
 *               registry.conf和file.conf
 *            配置spring.cloud.alibaba.seata.tx-service-group  vgroup_mapping.gulimall-ware-fescar-service-group
 *          5.启动测试分布式事务
 *          6.给分布式大事务的入口标注@GlobalTransaction
 *          7.每一个远程的小事务用@Transaction
 *
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {

        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}

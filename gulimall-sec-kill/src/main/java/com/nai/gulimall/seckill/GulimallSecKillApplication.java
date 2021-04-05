package com.nai.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1.整合Sentinel
 *   1).导入依赖 spring-cloud-starter-alibaba-sentinel
 *   2).下载sentinel控制台
 *   3).配置sentinel控制台地址信息
 *   4).在控制台调整所有的参数,[默认的所有流控设置存储在内存中,重启失效]
 * 2.每一个微服务导入 spring-boot-starter-actuator,并配置management.endpoints.web.exposure.include=*
 * 3.自定义sentinel流控返回数据
 * 4.使用sentinel来保护feign远程调用:熔断
 *    1).调用方的熔断保护 feign.sentinel.enabled=true
 *    2).在调用方,手动指定远程服务的降级策略 远程服务被降级处理.就会触发我们的熔断回调方法
 *    3).超大浏览的时候,必须牺牲一些远程服务.在服务的提供方(远程服务)指定降级测率
 *      提供方是在运行.但是不运行自己的业务逻辑,返回的是默认的降级数据(限流的数据)
 * 5.自定义受保护的资源
 *    1).代码 try (Entry entry = SphU.entry("secKillSkus")){
 *      //业务逻辑
 *    }catch(Exception e){}
 *    2).注解
 *   无论1.2方式,都要配置被限流后的默认返回
 *   URL限流可以设置统一返回
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSecKillApplication {

  public static void main(String[] args) {
    SpringApplication.run(GulimallSecKillApplication.class, args);
  }
}

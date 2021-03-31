package com.nai.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1.SpringSession核心原理:
 *  1).@EnableRedisHttpSession导入RedisHttpSerssionConfiguration配置
 *      1.给容器中添加了一个组件
 *          SessionRepository==>>RedisIndexedSessionRepository: redis操作session.session的增删改查的封装类
 *      2.SessionRepositoryFilter==>>Filter: session存储过滤器;每个请求过来都必须经过Filter
 *          1.创建的时候,就自动从容器中获取到了SessionRepository
 *          2.原生的request,response对象都被包装成了SessionRepositoryRequestWrapper,SessionRepositoryResponseWrapper
 *          3.以后获取session, request.getSession();
 *          4.wrappedRequest.getSession();===>>SessionRepository 中获取到的.
 *  装饰着模式;
 *      自动续期:redis中的数据也是有过期时间的
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession//整合redis 作为session存储
public class GulimallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthApplication.class, args);
    }

}

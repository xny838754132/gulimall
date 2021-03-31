package com.nai.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author TheNai
 * @create 2021-02-27 21:44
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对redis的使用都是通过RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1.创建配置
        Config config = new Config();
        //Redis url should start with redis:// or rediss:// (for SSL connection)
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        //2.根据config对象创建出RedissonClient实例
        return Redisson.create(config);
    }
}

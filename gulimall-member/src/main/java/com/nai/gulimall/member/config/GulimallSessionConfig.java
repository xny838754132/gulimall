package com.nai.gulimall.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * The type Gulimall session config.
 *
 * @author TheNai
 * @date 2021 -03-17 22:17 1.Spring-session依赖 2.spring-session配置 3.引入LoginInterceptor.WebMvcConfigure
 */
@Configuration
public class GulimallSessionConfig {

    /**
     * Cookie serializer cookie serializer.
     *
     * @return the cookie serializer
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULISESSION");

        return cookieSerializer;
    }

    /**
     * Spring session default redis serializer redis serializer.
     *
     * @return the redis serializer
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}

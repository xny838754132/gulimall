package com.nai.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The type Guli feign config.
 *
 * @author TheNai
 * @date 2021 -03-23 23:07
 */
@Slf4j
@Configuration
public class GuliFeignConfig {

    /**
     * Request interceptor request interceptor.
     *
     * @return the request interceptor
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //1.使用RequestContextHolder拿到刚进来的这个请求数据
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if (attributes != null) {
                    System.out.println("拦截器线程..." + Thread.currentThread().getId());
                    HttpServletRequest request = attributes.getRequest();
                    //老请求
                    //同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    //给新请求同步老请求的cookie
                    template.header("Cookie", cookie);
                }
            }
        };
    }
}

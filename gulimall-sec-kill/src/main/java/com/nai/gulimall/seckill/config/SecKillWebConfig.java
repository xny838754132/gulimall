package com.nai.gulimall.seckill.config;

import com.nai.gulimall.seckill.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecKillWebConfig implements WebMvcConfigurer {
  @Autowired LoginUserInterceptor loginUserInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
  }
}

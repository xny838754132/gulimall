package com.nai.gulimall.member.config;

import com.nai.gulimall.member.intercepetor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Member web config.
 *
 * @author TheNai
 * @date 2021 -03-31 20:19
 */
@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

  /**
   * The Login user interceptor.
   */
  @Autowired
  LoginUserInterceptor loginUserInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
  }
}

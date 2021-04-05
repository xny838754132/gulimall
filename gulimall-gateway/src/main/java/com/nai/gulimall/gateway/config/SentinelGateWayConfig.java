package com.nai.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.nai.gulimall.common.exception.BizCodeEnum;
import com.nai.gulimall.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGateWayConfig {

  /** TODO 响应式编程 */
  public SentinelGateWayConfig() {
    GatewayCallbackManager.setBlockHandler(
        new BlockRequestHandler() {
          /**
           * 网关先限流了请求,就会调用此回调 Mono Flux
           *
           * @param serverWebExchange
           * @param throwable
           * @return
           */
          @Override
          public Mono<ServerResponse> handleRequest(
              ServerWebExchange serverWebExchange, Throwable throwable) {
            R error =
                R.error(
                    BizCodeEnum.TO_MANY_REQUEST.getCode(),
                    BizCodeEnum.TO_MANY_REQUEST.getMessage());
            String errJson = JSON.toJSONString(error);
            return ServerResponse.ok().body(Mono.just(errJson), String.class);
          }
        });
  }
}

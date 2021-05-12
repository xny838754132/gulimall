package com.nai.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.nai.gulimall.order.config.AlipayTemplate;
import com.nai.gulimall.order.service.OrderService;
import com.nai.gulimall.order.vo.PayAsyncVo;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TheNai
 * @date 2021-03-31 22:42
 */
@RestController
public class OrderPayedListener {

  @Autowired
  OrderService orderService;

  @Autowired
  AlipayTemplate alipayTemplate;

  @PostMapping("/payed/notify")
  public String handleAliPayed(PayAsyncVo vo, HttpServletRequest request)
      throws AlipayApiException, UnsupportedEncodingException {
    //只要我们收到了支付宝给我们异步的通知,告诉我们订单支付成功,返回success,支付宝就不再通知
//        Map<String, String[]> map = request.getParameterMap();
//        for (String key : map.keySet()) {
//            String parameter = request.getParameter(key);
//            System.out.println("参数名" + key + "==>参数值" + parameter);
//        }
//        System.out.println("支付宝通知到位了...数据" + map);
    //验签
    Map<String, String> params = new HashMap<String, String>();
    Map<String, String[]> requestParams = request.getParameterMap();
    for (String name : requestParams.keySet()) {
      String[] values = (String[]) requestParams.get(name);
      String valueStr = "";
      for (int i = 0; i < values.length; i++) {
        valueStr = (i == values.length - 1) ? valueStr + values[i]
            : valueStr + values[i] + ",";
      }
      //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
      params.put(name, valueStr);
    }
    //调用SDK验证签名
    boolean signVerified = AlipaySignature
        .rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(),
            alipayTemplate.getSign_type());
    if (signVerified) {
      System.out.println("签名验证成功");
      return orderService.handlePayResult(vo);
    } else {
      System.out.println("签名验证失败");
      return "error";
    }
  }
}

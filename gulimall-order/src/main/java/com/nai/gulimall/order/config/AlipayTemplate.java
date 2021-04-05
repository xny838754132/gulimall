package com.nai.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.nai.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

  /** 在支付宝创建的应用的id */
  private String app_id = "2021000117631106";

  /** 商户私钥，您的PKCS8格式RSA2私钥 */
  private String merchant_private_key =
      "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCoXHYX0g/hchMv/coUn9KEC4kdjd6WDz+3bO9RJidqDLM3578XhB5RMUp1UD1ZrPIuePn9YswmeS/vqfMVy31KmmqislHL1kWotJNPyrpsQGhNeDNz6fK/E3diapITgwE6MX3SNn0l2aPGxLDpVhfJtZsaOVj+YFR/y/uzm47ER7pvjvoqNuhptjbOtibYz8b6ikQB0VQB3QJyLHuR6Fb/2hcpir/eXMIKQ1Q0UemH+LZioF1cD8yrat+1xRnumxVx+8c7QPfLczXlaFXwNIQGD4pM3PB5/flrDJFCIuMcwOjHyu+VrxWok2AqFBrexKKm1cIpPk5XeXazXA/R+0/nAgMBAAECggEAIG7XbRVRa9u+6ILaeAil1DwoqOHYnE3Jt3sryxUviJVzvPRO8qfH81osftb7Spgve1ZgyjhLHapA2smBg5RUyZdfrYHukfbwKmTG8BscuJfbv/jE6LKLydVlLTdcdpfJF73LAEDw8OaxTyw67GhiYsAyZeXgR/ikro3MKjhwU00xzlfJ6OxyMLJC7qVi2zdCgVjWsNilCSpvzYSFFGZArH7PNEHgCWM82PHEJb6R/xUx/lWDLHLHMQRkH50ry22PXtH24Tg2CxAIAq0qywbjGQd+s1BcnTsL2yiDHd6xOCGXPKLZL6zoOrwh/VcCfO5YnqhcZkKsDxejlp0su2VC8QKBgQDh5CuZkum6b6xKa5hO7p7xYABUcaG+xDASAEKa5//7cyzB8Ib4o6FRkMiM+CkCk5NBQ/ac7qqOUeM6KPDMlWA0Lhf0BH5N3oKjmjaarp3SKxLWFOdcRJ2fdovnozPD3LjrQEhkRcwZRmGj8XhgnhZvcHjwRK8R4nUsPZujsN1NLwKBgQC+zUHApmvLwD1QO+88dRZg1cgqdGY2U7pDE1q1UAhAkM3IVc6LaAFzrLUswLKMmzvEFr6lrrWcCOAiNuNw4eQtuRC0Y4fav2ATD19ydP9286cJxafBo0P7Vl+Qh7hz7nCDEhAAcWcs0Bbp/12fNDvOFvlMUGmivX5i8yFr73AqyQKBgQCoBRq1GYTGiwgBNvTMSUtqSLkWFotzpOQIj677H/PUqb+x5eHPEY8NZ3709CU06GYQlqfZ5OByCdDVUtyBn942Ec75T1pPO2G6pRtWhOmZCTbyjAvnIpaHpRq9SjRXzFazjVYLV6tPZX18/0BFxXD0pRkfxkbeK65yIMUixYxmOwKBgQCzuVwKP7iiP/6BKTN8Wz4tELJvax0sZ0m4DO8QgsaDKuLrpgfcQKe9ch1mfkyxLTfqyRHjtuHSjHmzQMiUd/MbLouRn39EiafRzFU31wUb4v6gsdIHD+blRMLXvT/908efbtAr2qY5C/nJiIWDhQwgYHuLauNWK/B0/Feq9+tMIQKBgHL/l/ZvVQ3XNn0t/0B9QETSwdzC2ida2mMFd6qFWNut4n5f56jmKMdgY2WYB7PGHKNqe2KIzOxd8lKq9UyCONh+lsj6UqBl7WE2q6aX3mFSgNYlWIiTny40/Db/Gz/DPQnK8eFahuyNgHKoWQdXANvdT0kas9YNuOa7ydAG80Y6";

  /** 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥 */
  private String alipay_public_key =
      "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkpgbt/37chhiLXV5LSXuhy7rHFqQpLOvClHln+xlI1SlpJf37wmJZFMF0yHzuQxlyhWa7lWe/yZaxiCLGMO/dc89gPUq7XLiQzxIVhnECSTxoKNeTyk1FG9U+7Iq6cAR8TOkBMlDCtzKEx02Teip++i51M2bYkD8gEpcwno5QhzhMeIa5D3tQqYHavnfezTqn1W176sZV/u05P7yl38z7uBt5NhGKARlCaOxiZkoLx7khq1EcSq/kCYEF18/L0YG4kqSs9uD7hjmIFnZcOxTRcMd6m2TOZWQismQ2kWY905N5lanvti622WZ4qzntVK8shXrM4Zvong5p3aoffsg/wIDAQAB";

  /** 服务器[异步通知]页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息 */
  private String notify_url = "http://hs6ldvn7fk.52http.tech/payed/notify";

  /** 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 同步通知，支付成功，一般跳转到成功页 */
  private String return_url = "http://member.gulimall.com/memberOrder.html";

  // 签名方式
  private String sign_type = "RSA2";

  // 字符编码格式
  private String charset = "utf-8";

  // 收单时间
  private String timeout = "30m";

  // 支付宝网关； https://openapi.alipaydev.com/gateway.do
  private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

  public String pay(PayVo vo) throws AlipayApiException {

    // AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl,
    // AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset,
    // AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type)
    // 1、根据支付宝的配置生成一个支付客户端
    AlipayClient alipayClient =
        new DefaultAlipayClient(
            gatewayUrl,
            app_id,
            merchant_private_key,
            "json",
            charset,
            alipay_public_key,
            sign_type);

    // 2、创建一个支付请求 //设置请求参数
    AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
    alipayRequest.setReturnUrl(return_url);
    alipayRequest.setNotifyUrl(notify_url);

    // 商户订单号，商户网站订单系统中唯一订单号，必填
    String out_trade_no = vo.getOut_trade_no();
    // 付款金额，必填
    String total_amount = vo.getTotal_amount();
    // 订单名称，必填
    String subject = vo.getSubject();
    // 商品描述，可空
    String body = vo.getBody();

    alipayRequest.setBizContent(
        "{\"out_trade_no\":\""
            + out_trade_no
            + "\","
            + "\"total_amount\":\""
            + total_amount
            + "\","
            + "\"subject\":\""
            + subject
            + "\","
            + "\"body\":\""
            + body
            + "\","
            + "\"timeout_express\":\""
            + timeout
            + "\","
            + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

    String result = alipayClient.pageExecute(alipayRequest).getBody();

    // 会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
    //        System.out.println("支付宝的响应：" + result)

    return result;
  }
}

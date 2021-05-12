package com.nai.gulimall.common.exception;

/**
 * 自定义错误码
 *
 * @author 83875
 */
public enum BizCodeEnum {

  UNKNOWN_EXCEPTION(10000, "系统未知异常"),
  VALID_EXCEPTION(10001, "参数格式校验失败"),
  TO_MANY_REQUEST(10002, "请求流量过大"),
  PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
  VALID_CODE_EXCEPTION(10002, "验证码获取频率太高,稍后再试"),
  USER_EXIST_EXCEPTION(15001, "用户存在"),
  PHONE_EXIST_EXCEPTION(15002, "手机号存在"),
  LOGIN_ACCT_PASSWORD_VALID_EXCEPTION(15003, "账号或密码错误"),
  NO_STOCK_EXCEPTION(21000, "商品库存不足"),
  ;

  private int code;

  private String message;

  BizCodeEnum(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

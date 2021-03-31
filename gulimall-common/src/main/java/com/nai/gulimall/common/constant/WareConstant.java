package com.nai.gulimall.common.constant;

public class WareConstant {
    public enum PurchaseEnum{

        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        RECEIVE(2,"已领取"),
        FINISH(3,"已完成"),
        HAVE_ERROR(4,"有异常");
        PurchaseEnum(int code,String message){
            this.code=code;
            this.message=message;
        }

        private int code;
        private String message;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
    public enum PurchaseDetailEnum{

        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUY(2,"正在采购"),
        FINISH(3,"已完成"),
        HAVE_ERROR(4,"采购失败");
        PurchaseDetailEnum(int code,String message){
            this.code=code;
            this.message=message;
        }

        private int code;
        private String message;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}

package com.nai.gulimall.common.constant;

/**
 * 商品属性
 * @author 83875
 */
public class ProductConstant {

    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0,"销售属性");

        AttrEnum(int code,String message){
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

    public enum StatusEnum{
        NEW_SPU(0,"新建"),
        SPU_UP(1,"商品上架"),
        SPU_DOWN(2,"商品下架");
        StatusEnum(int code,String message){
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

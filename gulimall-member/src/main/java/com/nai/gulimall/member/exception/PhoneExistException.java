package com.nai.gulimall.member.exception;

/**
 * @author TheNai
 * @date 2021-03-13 11:46
 */
public class PhoneExistException extends RuntimeException{

    public PhoneExistException() {
        super("手机号已存在");
    }
}

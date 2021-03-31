package com.nai.gulimall.member.exception;

/**
 * @author TheNai
 * @date 2021-03-13 11:46
 */
public class UserNameExistException extends RuntimeException{

    public UserNameExistException() {
        super("用户名已存在");
    }
}

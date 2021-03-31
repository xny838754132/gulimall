package com.nai.gulimall.order.constant;

/**
 * @author TheNai
 * @date 2021-03-25 21:24
 */
public class RedisConstant {
    /**
     * redis原子删除lua脚本
     */
    public static final String SCRIPT = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
}

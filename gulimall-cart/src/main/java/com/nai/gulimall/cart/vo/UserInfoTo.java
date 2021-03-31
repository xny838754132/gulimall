package com.nai.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author TheNai
 * @date 2021-03-20 15:03
 */
@Data
@ToString
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false;
}

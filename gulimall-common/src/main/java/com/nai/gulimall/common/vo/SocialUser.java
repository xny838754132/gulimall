package com.nai.gulimall.common.vo;

import lombok.Data;

/**
 * @author TheNai
 * @date 2021-03-15 22:41
 */
@Data
public class SocialUser {
    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}

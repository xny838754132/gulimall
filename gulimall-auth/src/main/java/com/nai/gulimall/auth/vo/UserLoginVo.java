package com.nai.gulimall.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author TheNai
 * @date 2021-03-13 13:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVo implements Serializable {
    private String loginAcct;
    private String password;
}

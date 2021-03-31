package com.nai.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRepoVo extends AttrVo{

    private String catalogName;

    private String groupName;

    private Long[] catalogPath;
}

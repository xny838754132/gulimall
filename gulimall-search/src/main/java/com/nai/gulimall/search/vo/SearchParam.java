package com.nai.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author TheNai
 * @create 2021-03-01 21:59
 */
@Data
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     * saleCount_asc/desc
     * skuPrice_asc/desc
     * hutScore_asc/desc
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock(是否有货),skuPrice(价格区间),brandId(),catalog3Id,attrs
     */
    private Integer hasStock;

    /**
     * 加个区间
     */
    private String skuPrice;

    /**
     * 品牌id
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;
}

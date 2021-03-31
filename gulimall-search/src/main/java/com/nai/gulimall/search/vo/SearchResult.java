package com.nai.gulimall.search.vo;


import com.nai.gulimall.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author TheNai
 * @date 2021-03-01 22:38
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;
    private List<Integer> pageNavs;

    private List<BrandVo> brands;

    private List<AttrVo> attrs;

    private List<CatalogVo> catalogs;



    /**
     * 面包屑导航
     */
    private List<NavVo> navs=new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();
    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}

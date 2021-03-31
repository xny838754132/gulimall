package com.nai.gulimall.search.service;

import com.nai.gulimall.search.vo.SearchParam;
import com.nai.gulimall.search.vo.SearchResult;

/**
 * @author TheNai
 * @create 2021-03-01 22:00
 */
public interface MallSearchService {

    /**
     *
     * @param searchParam 检索的所有参数
     * @return 检索的结果
     */
    SearchResult search(SearchParam searchParam);
}

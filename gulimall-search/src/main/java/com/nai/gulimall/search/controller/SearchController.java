package com.nai.gulimall.search.controller;

import com.nai.gulimall.search.service.MallSearchService;
import com.nai.gulimall.search.vo.SearchParam;
import com.nai.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author TheNai
 * @create 2021-03-01 21:25
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自动将请求提交过来的所有请求参数封装成对象
     *
     * @param searchParam
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {
        searchParam.set_queryString(request.getQueryString());
        //1.根据传递来的页面查询参数,去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}

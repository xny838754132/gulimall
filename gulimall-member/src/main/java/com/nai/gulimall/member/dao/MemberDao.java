package com.nai.gulimall.member.dao;

import com.nai.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author TheNai
 * @email TheNai@gmail.com
 * @date 2021-02-06 22:39:37
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}

package com.nai.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nai.gulimall.common.utils.HttpUtils;
import com.nai.gulimall.common.utils.PageUtils;
import com.nai.gulimall.common.utils.Query;
import com.nai.gulimall.common.vo.SocialUser;
import com.nai.gulimall.member.dao.MemberDao;
import com.nai.gulimall.member.dao.MemberLevelDao;
import com.nai.gulimall.member.entity.MemberEntity;
import com.nai.gulimall.member.entity.MemberLevelEntity;
import com.nai.gulimall.member.exception.PhoneExistException;
import com.nai.gulimall.member.exception.UserNameExistException;
import com.nai.gulimall.member.service.MemberService;
import com.nai.gulimall.member.vo.MemberLoginVo;
import com.nai.gulimall.member.vo.MemberRegistryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void registry(MemberRegistryVo registryVo) {
        MemberEntity entity = new MemberEntity();
        //1.设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());
        //检查用户名和手机号 是否唯一 为了让controller能感知异常,异常机制
        checkPhoneUnique(registryVo.getPhone());
        checkUserNameUnique(registryVo.getUserName());

        entity.setMobile(registryVo.getPhone());
        entity.setUsername(registryVo.getUserName());
        entity.setNickname(registryVo.getUserName());
        //设置密码 进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(registryVo.getPassword());
        entity.setPassword(encode);

        baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        Integer username = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo loginVo) {
        String loginAcct = loginVo.getLoginAcct();
        String password = loginVo.getPassword();
        //1.去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAcct)
                .or().eq("mobile", loginAcct));
        if (entity == null) {
            return null;
        } else {
            //获取到数据库的password
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        //具有登录和注册合并逻辑
        String uid = socialUser.getUid();
        //1.判断当前社交用户是否已经登陆过系统
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //这个用户已经注册
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            baseMapper.updateById(update);
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            //2.没有查询到当前社交用户对应的记录,我们就需要注册
            MemberEntity registry = new MemberEntity();
            try {
                //3.查询当前社交用户的社交账号信息,昵称、性别等。。。
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccess_token());
                query.put("uid", socialUser.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    //.....
                    registry.setNickname(name);
                    registry.setGender("m".equals(gender) ? 1 : 0);
                }
            } catch (Exception e) {
                log.info("获取社交登录信息失败：{}", e.getMessage());
            }
            registry.setSocialUid(socialUser.getUid());
            registry.setAccessToken(socialUser.getAccess_token());
            registry.setExpiresIn(socialUser.getExpires_in());
            baseMapper.insert(registry);
            return registry;
        }
    }
}
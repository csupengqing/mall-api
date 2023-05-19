package org.csu.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.domain.User;
import org.csu.api.dto.CheckUserFieldDTO;
import org.csu.api.dto.LoginUserDTO;
import org.csu.api.dto.RegisterUserDTO;
import org.csu.api.persistence.UserMapper;
import org.csu.api.service.UserService;
import org.csu.api.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private Cache<String, String> localCache;

    @Override
    public CommonResponse<UserVO> login(LoginUserDTO loginUserDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUserDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        //用户名没有查询到，返回登录错误
        if (user == null) {
            return CommonResponse.createForError("用户名或密码错误");
        }

        if (bCryptPasswordEncoder.matches(loginUserDTO.getPassword(), user.getPassword())) {
            //登录成功
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return CommonResponse.createForSuccess("登录成功", userVO);
        }
        //密码错误
        return CommonResponse.createForError("用户名或密码错误");
    }

    @Override
    public CommonResponse<Object> checkField(String fieldName, String fieldValue) {
        if (StringUtils.equals(fieldName, CONSTANT.USER_FIELD.USERNAME)) {
            long rows = userMapper.selectCount(Wrappers.<User>query().eq("username", fieldValue));
            if (rows > 0) {
                return CommonResponse.createForError("用户名已存在");
            }
        } else if (StringUtils.equals(fieldName, CONSTANT.USER_FIELD.PHONE)) {
            long rows = userMapper.selectCount(Wrappers.<User>query().eq("phone", fieldValue));
            if (rows > 0) {
                return CommonResponse.createForError("电话号码已存在");
            }
        } else if (StringUtils.equals(fieldName, CONSTANT.USER_FIELD.EMAIL)) {
            long rows = userMapper.selectCount(Wrappers.<User>query().eq("email", fieldValue));
            if (rows > 0) {
                return CommonResponse.createForError("邮箱已存在");
            }
        } else {
            return CommonResponse.createForError("参数错误");
        }
        return CommonResponse.createForSuccess();
    }

    @Override
    public CommonResponse<Object> register(RegisterUserDTO registerUserDTO) {

        CommonResponse<Object> checkResult = checkField(CONSTANT.USER_FIELD.USERNAME, registerUserDTO.getUsername());
        if(!checkResult.isSuccess()){
            return checkResult;
        }
        checkResult = checkField(CONSTANT.USER_FIELD.EMAIL, registerUserDTO.getEmail());
        if(!checkResult.isSuccess()){
            return checkResult;
        }
        checkResult = checkField(CONSTANT.USER_FIELD.PHONE, registerUserDTO.getPhone());
        if(!checkResult.isSuccess()){
            return checkResult;
        }

        User registerUser = new User();
        BeanUtils.copyProperties(registerUserDTO, registerUser);

        registerUser.setPassword(bCryptPasswordEncoder.encode(registerUser.getPassword()));

        registerUser.setRole(CONSTANT.ROLE.CUSTOMER);
        registerUser.setCreateTime(LocalDateTime.now());
        registerUser.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.insert(registerUser);
        if(rows == 0){
            return CommonResponse.createForError("注册用户失败");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(registerUser, userVO);

        return CommonResponse.createForSuccess("注册用户成功" , userVO);
    }

    @Override
    public CommonResponse<String> getForgetQuestion(String username) {
        CommonResponse<Object> checkResult = this.checkField(CONSTANT.USER_FIELD.USERNAME,username);
        if(checkResult.isSuccess()){
            return CommonResponse.createForError("该用户名不存在");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);

        String question = userMapper.selectOne(Wrappers.<User>query().eq("username",username)).getQuestion();
        if(StringUtils.isNotBlank(question)){
            return CommonResponse.createForSuccess(question);
        }
        return CommonResponse.createForError("密码问题为空");
    }

    @Override
    public CommonResponse<String> checkForgetAnswer(String username, String question, String answer) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username).eq("question", question).eq("answer",answer);
        long rows = userMapper.selectCount(queryWrapper);

        //rows>0表示忘记密码的问题答案正确
        if(rows > 0){
            //使用UUID生成一段token字符串
            String forgetToken = UUID.randomUUID().toString();
            //将生成的token放入本地CaffeineCache缓存中，用户名为key，token为value，失效时间为5分钟
            localCache.put(username,forgetToken);
            //输出日志，记录存入缓存成功，打印时间
            log.info("Put into LocalCache: ({},{}), {}",username, forgetToken , LocalDateTime.now());
            return CommonResponse.createForSuccess(forgetToken);
        }
        return CommonResponse.createForError("重置密码的问题答案错误");
    }

    @Override
    public CommonResponse<String> resetForgetPassword(String username, String newPassword, String forgetToken) {
        CommonResponse<Object> checkResult = this.checkField(CONSTANT.USER_FIELD.USERNAME, username);
        if (checkResult.isSuccess()) {
            return CommonResponse.createForError("用户名不存在");
        }

        //从本地缓存中取出之前存入的token
        String token = localCache.getIfPresent(username);
        //输出日志记录取出token成功
        log.info("Get token from LocalCache : ({},{})" ,username, token);
        if (StringUtils.isBlank(token)) {
            return CommonResponse.createForError("token无效或已过期");
        }

        if (StringUtils.equals(token, forgetToken)) {
            //对重置的新密码进行MD5加密
            String md5Password = bCryptPasswordEncoder.encode(newPassword);

            User user = new User();
            user.setUsername(username);
            user.setPassword(md5Password);

            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("username", username);
            updateWrapper.set("password", user.getPassword());
            int rows = userMapper.update(user, updateWrapper);

            if (rows > 0) {
                return CommonResponse.createForSuccessMessage("重置密码成功");
            }
            return CommonResponse.createForError("通过忘记密码问题答案，重置密码失败,请重新获取token");
        } else {
            return CommonResponse.createForError("token错误，请重新获取token");
        }
    }
}

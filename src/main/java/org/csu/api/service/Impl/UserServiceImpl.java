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
import org.csu.api.common.ResponseCode;
import org.csu.api.domain.User;
import org.csu.api.dto.*;
import org.csu.api.persistence.UserMapper;
import org.csu.api.service.UserService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    @Override
    public CommonResponse<String> resetPassword(Integer id, ResetPasswordDTO resetPasswordDTO) {
        //检验旧密码
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("id", id);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(), "重设密码失败");
        }
        if (bCryptPasswordEncoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())) {
            //重置
            String md5Password = bCryptPasswordEncoder.encode(resetPasswordDTO.getNewPassword());
            User newUser = new User();
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id);
            updateWrapper.set("password", md5Password);
            int rows = userMapper.update(newUser, updateWrapper);
            if (rows > 0) {
                return CommonResponse.createForSuccessMessage("SUCCESS");
            }
        }
        return CommonResponse.createForError("重设密码失败");
    }

    @Override
    public CommonResponse<Object> updateUserInfo(UpdateUserDTO updateUserDTO) {
        //用户是否存在
        User user = userMapper.selectById(updateUserDTO.getId());
        if(user == null)
            return CommonResponse.createForError("用户不存在");

        //用户名、邮箱、电话校验
        if(!Objects.equals(updateUserDTO.getUsername(), user.getUsername())){
            CommonResponse<Object> checkResult = checkField(CONSTANT.USER_FIELD.USERNAME, updateUserDTO.getUsername());
            if(!checkResult.isSuccess()){
                return checkResult;
            }
        }
        if(!Objects.equals(updateUserDTO.getEmail(), user.getEmail())){
            CommonResponse<Object> checkResult = checkField(CONSTANT.USER_FIELD.EMAIL, updateUserDTO.getEmail());
            if(!checkResult.isSuccess()){
                return checkResult;
            }
        }
        if(!Objects.equals(updateUserDTO.getPhone(), user.getPhone())){
            CommonResponse<Object> checkResult = checkField(CONSTANT.USER_FIELD.PHONE, updateUserDTO.getPhone());
            if(!checkResult.isSuccess()){
                return checkResult;
            }
        }
        //更新
        String md5Password = bCryptPasswordEncoder.encode(updateUserDTO.getPassword());
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", updateUserDTO.getId());
        updateWrapper.set("username", updateUserDTO.getUsername())
                .set("password", md5Password)
                .set("email", updateUserDTO.getEmail())
                .set("phone", updateUserDTO.getPhone())
                .set("question", updateUserDTO.getQuestion())
                .set("answer", updateUserDTO.getAnswer());
        int rows = userMapper.update(user, updateWrapper);
        if (rows > 0) {
            return CommonResponse.createForSuccess("SUCCESS");
        }
        return CommonResponse.createForError("修改个人信息失败");
    }

    @Override
    public CommonResponse<String> deleteUser(String ids) {
        String[] idList = ids.split(",");
        int result = 0;
        for(String id:idList){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id",Integer.parseInt(id)).eq("role",CONSTANT.ROLE.CUSTOMER);
            if(userMapper.selectOne(queryWrapper) == null)
                return CommonResponse.createForError("该用户不存在或为管理员，删除用户失败");
            result += userMapper.deleteById(id);
        }
        if(result == idList.length)
            return CommonResponse.createForSuccessMessage("删除用户成功");
        return CommonResponse.createForError("删除用户失败");
    }

    @Override
    public CommonResponse<List<User>> listUser() {
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("role",CONSTANT.ROLE.CUSTOMER);
        List<User> userList = userMapper.selectList(queryWrapper);
        return CommonResponse.createForSuccess("SUCCESS",userList);
    }

    @Override
    public CommonResponse<User> getUserInfo(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null)
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(), ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        else
            return CommonResponse.createForSuccess("SUCCESS",user);
    }
}

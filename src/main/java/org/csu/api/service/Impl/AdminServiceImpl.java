package org.csu.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.domain.User;
import org.csu.api.dto.LoginUserDTO;
import org.csu.api.dto.RegisterUserDTO;
import org.csu.api.dto.ResetPasswordDTO;
import org.csu.api.dto.UpdateUserInfoDTO;
import org.csu.api.persistence.UserMapper;
import org.csu.api.service.AdminService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("adminService")
@Slf4j
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserMapper userMapper;
    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;
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

        registerUser.setRole(CONSTANT.ROLE.ADMIN);
        registerUser.setCreateTime(LocalDateTime.now());
        registerUser.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.insert(registerUser);
        if(rows == 0){
            return CommonResponse.createForError("新增管理员失败");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(registerUser, userVO);

        return CommonResponse.createForSuccess("新增管理员成功" , userVO);
    }

    @Override
    public CommonResponse<UserVO> login(LoginUserDTO loginUserDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUserDTO.getUsername()).eq("role",CONSTANT.ROLE.ADMIN);
        User user = userMapper.selectOne(queryWrapper);
        //管理员没有查询到，返回登录错误
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
    public CommonResponse<String> resetPassword(Integer id, ResetPasswordDTO resetPasswordDTO) {
        //检验旧密码
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("role",CONSTANT.ROLE.ADMIN);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(), "重设密码失败");
        }
        if (bCryptPasswordEncoder.matches(resetPasswordDTO.getOldPassword(), user.getPassword())) {
            //重置
            String md5Password = bCryptPasswordEncoder.encode(resetPasswordDTO.getNewPassword());
            User newUser = new User();
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id).eq("role",CONSTANT.ROLE.ADMIN);
            updateWrapper.set("password", md5Password);
            int rows = userMapper.update(newUser, updateWrapper);
            if (rows > 0) {
                return CommonResponse.createForSuccessMessage("SUCCESS");
            }
        }
        return CommonResponse.createForError("重设密码失败");
    }


    @Override
    public CommonResponse<String> updateAdminInfo(Integer id, UpdateUserInfoDTO updateUserInfoDTO) {

        String md5Password = bCryptPasswordEncoder.encode(updateUserInfoDTO.getPassword());
        User user = new User();
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("role",CONSTANT.ROLE.ADMIN);
        updateWrapper.set("username", updateUserInfoDTO.getUsername())
                .set("password", md5Password)
                .set("email", updateUserInfoDTO.getEmail())
                .set("phone", updateUserInfoDTO.getPhone())
                .set("question", updateUserInfoDTO.getQuestion())
                .set("answer", updateUserInfoDTO.getAnswer());
        int rows = userMapper.update(user, updateWrapper);
        if (rows > 0) {
            return CommonResponse.createForSuccess("SUCCESS");
        }
        return CommonResponse.createForError("修改个人信息失败");
    }
}

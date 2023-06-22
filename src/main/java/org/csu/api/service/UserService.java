package org.csu.api.service;

import org.csu.api.common.CommonResponse;
import org.csu.api.domain.User;
import org.csu.api.dto.*;
import org.csu.api.vo.UserVO;

import java.util.List;

public interface UserService {

    //用户登录
    CommonResponse<UserVO> login(LoginUserDTO loginUserDTO);

    //用户注册时的字段校验接口
    CommonResponse<Object> checkField(String fieldName, String fieldValue);

    //用户注册
    CommonResponse<Object> register(RegisterUserDTO registerUserDTO);


    //获取忘记密码时的问题
    CommonResponse<String> getForgetQuestion(String username);

    //重置密码时验证密码问题答案是否正确
    CommonResponse<String> checkForgetAnswer(String username, String question,String answer);

    //根据token重置用户密码
    CommonResponse<String> resetForgetPassword(String username, String newPassword, String forgetToken);

    //登录状态重置密码
    CommonResponse<String> resetPassword(Integer id, ResetPasswordDTO resetPasswordDTO);

    //登录状态修改用户信息
    CommonResponse<Object> updateUserInfo(UpdateUserDTO updateUserDTO);

    //删除用户————管理员权限
    CommonResponse<String> deleteUser(Integer id);

    //查看所有用户信息————管理员权限
    CommonResponse<List<User>> listUser();

    //查看用户信息————管理员权限
    CommonResponse<User> getUserInfo(Integer id);
}

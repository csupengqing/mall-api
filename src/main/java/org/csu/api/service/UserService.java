package org.csu.api.service;

import org.csu.api.common.CommonResponse;
import org.csu.api.domain.User;
import org.csu.api.dto.CheckUserFieldDTO;
import org.csu.api.dto.LoginUserDTO;
import org.csu.api.dto.RegisterUserDTO;
import org.csu.api.vo.UserVO;

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
}

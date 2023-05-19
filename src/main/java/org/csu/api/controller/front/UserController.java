package org.csu.api.controller.front;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.domain.User;
import org.csu.api.dto.*;
import org.csu.api.service.UserService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private UserService userService;

    //DTO:客户端提交数据时数据的封装对象，以及各个层之间传递数据
    //VO：View Object，服务端向客户端返回数据时的封装对象
    //VO：Value Object，业务逻辑层和DAO层交换的封装对象
    //BO：业务对象，领域对象DO

    @PostMapping("login")
    public CommonResponse<UserVO> login(@Valid @RequestBody LoginUserDTO loginUserDTO,
                                        HttpSession session) {
        CommonResponse<UserVO> result = userService.login(loginUserDTO);
        if (result.isSuccess()) {
            session.setAttribute(CONSTANT.LOGIN_USER, result.getData());
        }
        return result;
    }

    @PostMapping("check_field")
    public CommonResponse<Object> checkField(@Valid @RequestBody CheckUserFieldDTO checkUserFieldDTO) {
        return userService.checkField(checkUserFieldDTO.getFieldName(), checkUserFieldDTO.getFieldValue());
    }

    @PostMapping("register")
    public CommonResponse<Object> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        if(!StringUtils.equals(registerUserDTO.getPassword(),registerUserDTO.getConfirmPassword())){
            return CommonResponse.createForError("两次密码不一致");
        }
        return userService.register(registerUserDTO);
    }

    @GetMapping("get_forget_question")
    public CommonResponse<String> getForgetQuestion(
            @RequestParam @NotBlank(message = "用户名不能为空") String username) {
        return userService.getForgetQuestion(username);
    }

    @PostMapping("check_forget_answer")
    public CommonResponse<String> checkForgetAnswer(
            @Valid @RequestBody CheckAnswerUserDTO checkAnswerUserDTO){
        return userService.checkForgetAnswer(
                checkAnswerUserDTO.getUsername(), checkAnswerUserDTO.getQuestion(), checkAnswerUserDTO.getAnswer());
    }

    @PostMapping("reset_forget_password")
    public CommonResponse<String> resetForgetPassword(
            @Valid @RequestBody ResetUserDTO resetUserDTO){
        return userService.resetForgetPassword(
                resetUserDTO.getUsername(), resetUserDTO.getNewPassword(), resetUserDTO.getForgetToken());
    }

    @PostMapping("logout")
    public CommonResponse<Object> logout(HttpSession session) {
        session.removeAttribute(CONSTANT.LOGIN_USER);
        return CommonResponse.createForSuccess("退出登录成功");
    }

}

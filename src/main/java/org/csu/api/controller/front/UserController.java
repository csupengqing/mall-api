package org.csu.api.controller.front;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.dto.*;
import org.csu.api.service.UserService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private UserService userService;

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

    @PostMapping("get_user_detail")
    public CommonResponse<UserVO> getUserDetail(HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_USER);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"用户未登录");
        }
        else return CommonResponse.createForSuccess(userVO);
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
    @PostMapping("/reset_password")
    public CommonResponse<String> reset_password(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
                                                 HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if(userVO == null){
            return CommonResponse.createForError("重设密码失败");
        }
        else return userService.resetPassword(userVO.getUsername(),resetPasswordDTO);
    }

    @PostMapping("/update_user_info")
    public CommonResponse<String> update_user_info(@Valid @RequestBody UpdateUserInfoDTO updateUserInfoDTO,
                                                   HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if(userVO == null){
            return CommonResponse.createForError("修改个人信息失败");
        }
        else{
            CommonResponse<String> result = userService.updateUserInfo(userVO.getUsername(), updateUserInfoDTO);
            if(result.isSuccess()){
                //成功则更新session
                LoginUserDTO loginUserDTO = new LoginUserDTO();
                loginUserDTO.setUsername(updateUserInfoDTO.getUsername());
                loginUserDTO.setPassword(updateUserInfoDTO.getPassword());
                CommonResponse<UserVO> result2 = userService.login(loginUserDTO);
                if (result2.isSuccess()) {
                    session.setAttribute(CONSTANT.LOGIN_USER, result2.getData());
                }
                return CommonResponse.createForSuccessMessage("SUCCESS");
            }
            else return CommonResponse.createForError("修改个人信息失败");
        }
    }

    @PostMapping("logout")
    public CommonResponse<String> logout(HttpSession session) {
        session.removeAttribute(CONSTANT.LOGIN_USER);
        return CommonResponse.createForSuccess("退出登录成功");
    }

}

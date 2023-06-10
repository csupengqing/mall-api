package org.csu.api.controller.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.dto.LoginUserDTO;
import org.csu.api.dto.RegisterUserDTO;
import org.csu.api.dto.ResetPasswordDTO;
import org.csu.api.dto.UpdateUserInfoDTO;
import org.csu.api.service.AdminService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {
    @Autowired
    private AdminService adminService;

    //用于向数据库加数据
    @PostMapping("/register")
    public CommonResponse<Object> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        if(!StringUtils.equals(registerUserDTO.getPassword(),registerUserDTO.getConfirmPassword())){
            return CommonResponse.createForError("两次密码不一致");
        }
        return adminService.register(registerUserDTO);
    }

    @PostMapping("/login")
    public CommonResponse<UserVO> adminLogin(@Valid @RequestBody LoginUserDTO loginUserDTO,
                                             HttpSession session){
        CommonResponse<UserVO> result = adminService.login(loginUserDTO);
        if (result.isSuccess()) {
            session.setAttribute(CONSTANT.LOGIN_ADMIN, result.getData());
        }
        return result;
    }

    @PostMapping("/get_admin_detail")
    public CommonResponse<UserVO> getUserDetail(HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else return CommonResponse.createForSuccess(userVO);
    }

    @PostMapping("/reset_password")
    public CommonResponse<String> reset_password(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
                                                 HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError("重设密码失败");
        }
        else return adminService.resetPassword(userVO.getId(),resetPasswordDTO);
    }

    @PostMapping("/update_admin_info")
    public CommonResponse<String> update_user_info(@Valid @RequestBody UpdateUserInfoDTO updateUserInfoDTO,
                                                   HttpSession session){
        UserVO userVO = (UserVO) session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if (userVO != null) {
            CommonResponse<String> result = adminService.updateAdminInfo(userVO.getId(), updateUserInfoDTO);
            if (result.isSuccess()) {
                //成功则更新session
                LoginUserDTO loginUserDTO = new LoginUserDTO();
                loginUserDTO.setUsername(updateUserInfoDTO.getUsername());
                loginUserDTO.setPassword(updateUserInfoDTO.getPassword());
                CommonResponse<UserVO> result2 = adminService.login(loginUserDTO);
                if (result2.isSuccess()) {
                    session.setAttribute(CONSTANT.LOGIN_ADMIN, result2.getData());
                    return CommonResponse.createForSuccess("SUCCESS");
                }
            }
        }
        return CommonResponse.createForError("修改管理员信息失败");
    }
    @PostMapping("/logout")
    public CommonResponse<String> logout(HttpSession session) {
        session.removeAttribute(CONSTANT.LOGIN_ADMIN);
        return CommonResponse.createForSuccess("退出登录成功");
    }
}

package org.csu.api.controller.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.domain.User;
import org.csu.api.dto.RegisterUserDTO;
import org.csu.api.dto.UpdateUserDTO;
import org.csu.api.service.UserService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//
//@CrossOrigin(origins = "http://localhost:8080",allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/admin/user")
@Validated
public class AdminUserController {
    @Autowired
    private UserService userService;

    //删除用户
    @PostMapping("/delete_user")
    public CommonResponse<String> deleteUser(@RequestParam @NotNull(message = "用户ID不能为空") String ids, HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return userService.deleteUser(ids);
    }
    //查看所有用户
    @GetMapping("/list_user")
    public CommonResponse<List<User>> listUser(HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        System.out.println(session.getAttribute(CONSTANT.LOGIN_ADMIN));
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return userService.listUser();
    }
    //查看某个用户信息
    @GetMapping("/find")
    public CommonResponse<User> getUserInfo(@RequestParam @NotNull(message = "用户名为空") String username,
                                            HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return userService.getUserInfo(username);
    }
    //修改用户信息
    @PostMapping("/update_user_info")
    public CommonResponse<Object> updateUserInfo(@Valid @RequestBody UpdateUserDTO updateUserDTO,
                                            HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return userService.updateUserInfo(updateUserDTO);
    }
    //新增用户
    @PostMapping("/addUser")
    public CommonResponse<Object> addUser(@Valid @RequestBody RegisterUserDTO registerUserDTO,
                                                 HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return userService.register(registerUserDTO);
    }
}

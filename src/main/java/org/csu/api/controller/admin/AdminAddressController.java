package org.csu.api.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.service.AddressService;
import org.csu.api.vo.AddressVO;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//
//@CrossOrigin(origins = "http://localhost:8080",allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/admin/address")
@Validated
public class AdminAddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public CommonResponse<List<AddressVO>> list(@RequestParam(required = false) Integer UserId,HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else return addressService.list(UserId);
    }

    @GetMapping("/delete")
    public CommonResponse<String> delete(@RequestParam Integer addressId, HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else return addressService.delete(addressId);
    }
}

package org.csu.api.controller.front;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.dto.AddAdressDTO;
import org.csu.api.dto.UpdateAdressDTO;
import org.csu.api.service.AdressService;
import org.csu.api.vo.AdressVO;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdressController {

    @Autowired
    private AdressService adressService;

    @PostMapping("/address/add")
    public CommonResponse<AdressVO> addAdress(@Valid @RequestBody AddAdressDTO addAdressDTO,
                                              HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return adressService.addAdress(
                addAdressDTO.getAddressName(),
                addAdressDTO.getAddressPhone(),
                addAdressDTO.getAddressMobile(),
                addAdressDTO.getAddressProvince(),
                addAdressDTO.getAddressCity(),
                addAdressDTO.getAddressDistrict(),
                addAdressDTO.getAddressDetail(),
                addAdressDTO.getAddressZip(),
                loginUser.getId());
    }

    @PostMapping("/address/delete")
    public CommonResponse<Object> deleteAdress(Integer addressId, HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return adressService.deleteAdress(addressId, loginUser.getId());
    }

    @PostMapping("/address/update")
    public CommonResponse<AdressVO> updateAdress(@Valid @RequestBody UpdateAdressDTO updateAdressDTO,
                                              HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return adressService.updateAdress(
                updateAdressDTO.getId(),
                updateAdressDTO.getAddressName(),
                updateAdressDTO.getAddressPhone(),
                updateAdressDTO.getAddressMobile(),
                updateAdressDTO.getAddressProvince(),
                updateAdressDTO.getAddressCity(),
                updateAdressDTO.getAddressDistrict(),
                updateAdressDTO.getAddressDetail(),
                updateAdressDTO.getAddressZip(),
                loginUser.getId());
    }

    @GetMapping("/address/find")
    public CommonResponse<AdressVO> findAdress(Integer addressId, HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return adressService.findAdress(addressId);
    }

    @GetMapping("/address/list")
    public CommonResponse<List<AdressVO>> listAdress(HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return adressService.listAdress(loginUser.getId());
    }

}

package org.csu.api.service;

import org.csu.api.common.CommonResponse;
import org.csu.api.dto.LoginUserDTO;
import org.csu.api.dto.RegisterUserDTO;
import org.csu.api.dto.ResetPasswordDTO;
import org.csu.api.dto.UpdateUserDTO;
import org.csu.api.vo.UserVO;

public interface AdminService {

    CommonResponse<Object> register(RegisterUserDTO registerUserDTO);

    CommonResponse<Object> checkField(String fieldName, String fieldValue);

    CommonResponse<UserVO> login(LoginUserDTO loginUserDTO);

    CommonResponse<String> resetPassword(Integer id, ResetPasswordDTO resetPasswordDTO);

    CommonResponse<String> updateAdminInfo(UpdateUserDTO updateUserDTO);
}

package org.csu.api.service;

import org.csu.api.common.CommonResponse;
import org.csu.api.dto.AddAddressDTO;
import org.csu.api.dto.UpdateAddressDTO;
import org.csu.api.vo.AddressVO;

import java.util.List;

public interface AddressService {
    CommonResponse<AddressVO> addAddress(Integer userId, AddAddressDTO addAddressDTO);

    CommonResponse<Object> deleteAddress(Integer addressId, Integer userId);

    CommonResponse<AddressVO> updateAddress(UpdateAddressDTO updateAddressDTO);

    CommonResponse<AddressVO> findAddress(Integer addressId);

    CommonResponse<List<AddressVO>> listAddress(Integer userId);
}

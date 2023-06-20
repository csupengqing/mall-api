package org.csu.api.service;

import org.csu.api.common.CommonResponse;
import org.csu.api.vo.AdressVO;

import java.util.List;

public interface AdressService {
    CommonResponse<AdressVO> addAdress(String addressName,
                                       String addressPhone,
                                       String addressMobile,
                                       String addressProvince,
                                       String addressCity,
                                       String addressDistrict,
                                       String addressDetail,
                                       String addressZip,
                                       Integer userId);

    CommonResponse<Object> deleteAdress(Integer addressId, Integer userId);

    CommonResponse<AdressVO> updateAdress(
                                       Integer id,
                                       String addressName,
                                       String addressPhone,
                                       String addressMobile,
                                       String addressProvince,
                                       String addressCity,
                                       String addressDistrict,
                                       String addressDetail,
                                       String addressZip,
                                       Integer userId);

    CommonResponse<AdressVO> findAdress(Integer addressId);

    CommonResponse<List<AdressVO>> listAdress(Integer userId);
}

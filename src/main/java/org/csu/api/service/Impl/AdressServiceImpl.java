package org.csu.api.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.csu.api.common.CommonResponse;
import org.csu.api.domain.Adress;
import org.csu.api.persistence.AdressMapper;
import org.csu.api.service.AdressService;
import org.csu.api.util.ListBeanUtils;
import org.csu.api.vo.AdressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdressServiceImpl implements AdressService {

    @Autowired
    private AdressMapper adressMapper;

    @Override
    public CommonResponse<AdressVO> addAdress(String addressName, String addressPhone, String addressMobile,
                                              String addressProvince, String addressCity, String addressDistrict,
                                              String addressDetail, String addressZip, Integer userId) {
        Adress adress = new Adress();
        adress.setUserId(userId);
        adress.setAddressName(addressName);
        adress.setAddressPhone(addressPhone);
        adress.setAddressMobile(addressMobile);
        adress.setAddressProvince(addressProvince);
        adress.setAddressCity(addressCity);
        adress.setAddressDistrict(addressDistrict);
        adress.setAddressDetail(addressDetail);
        adress.setAddressZip(addressZip);
        adress.setCreateTime(LocalDateTime.now());
        adress.setUpdateTime(LocalDateTime.now());
        int rows = adressMapper.insert(adress);
        if (rows == 0) {
            return CommonResponse.createForError("插入数据库失败");
        }

        AdressVO adressVO = new AdressVO();
        BeanUtils.copyProperties(adress, adressVO);
        return CommonResponse.createForSuccess(adressVO);
    }

    @Override
    public CommonResponse<Object> deleteAdress(Integer addressId, Integer userId) {
        QueryWrapper<Adress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", addressId);
        adressMapper.delete(queryWrapper);
        return CommonResponse.createForSuccess("SUCCESS");
    }

    @Override
    public CommonResponse<AdressVO> updateAdress(Integer id, String addressName, String addressPhone,
                                                 String addressMobile, String addressProvince, String addressCity,
                                                 String addressDistrict, String addressDetail, String addressZip, Integer userId) {
        Adress adress = new Adress();
        adress.setId(id);
        adress.setUserId(userId);
        adress.setAddressName(addressName);
        adress.setAddressPhone(addressPhone);
        adress.setAddressMobile(addressMobile);
        adress.setAddressProvince(addressProvince);
        adress.setAddressCity(addressCity);
        adress.setAddressDistrict(addressDistrict);
        adress.setAddressDetail(addressDetail);
        adress.setAddressZip(addressZip);
        adress.setCreateTime(LocalDateTime.now());
        adress.setUpdateTime(LocalDateTime.now());
        UpdateWrapper<Adress> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("user_id",userId);
        int rows = adressMapper.update(adress, updateWrapper);
        if (rows == 0) {
            return CommonResponse.createForError("更新数据库失败");
        }

        AdressVO adressVO = new AdressVO();
        BeanUtils.copyProperties(adress, adressVO);
        return CommonResponse.createForSuccess(adressVO);
    }

    @Override
    public CommonResponse<AdressVO> findAdress(Integer addressId) {
        Adress adress = adressMapper.selectById(addressId);
        AdressVO adressVO = new AdressVO();
        BeanUtils.copyProperties(adress, adressVO);
        return CommonResponse.createForSuccess(adressVO);
    }

    @Override
    public CommonResponse<List<AdressVO>> listAdress(Integer userId) {
        QueryWrapper<Adress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Adress> adressList = adressMapper.selectList(queryWrapper);
        List<AdressVO> adressVOList = ListBeanUtils.copyListProperties(adressList, AdressVO::new);
        return CommonResponse.createForSuccess(adressVOList);
    }
}

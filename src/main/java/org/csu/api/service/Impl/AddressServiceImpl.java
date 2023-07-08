package org.csu.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.checkerframework.checker.units.qual.A;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.domain.Address;
import org.csu.api.dto.AddAddressDTO;
import org.csu.api.dto.UpdateAddressDTO;
import org.csu.api.persistence.AddressMapper;
import org.csu.api.service.AddressService;
import org.csu.api.util.ListBeanUtils;
import org.csu.api.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public CommonResponse<AddressVO> addAddress(Integer userId, AddAddressDTO addAddressDTO) {
        Address address = new Address();
        BeanUtils.copyProperties(addAddressDTO,address);
        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        int rows = addressMapper.insert(address);
        if (rows == 0) {
            return CommonResponse.createForError("添加地址失败");
        }

        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(address, addressVO);
        return CommonResponse.createForSuccess(addressVO);
    }

    @Override
    public CommonResponse<Object> deleteAddress(Integer addressId, Integer userId) {
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", addressId).eq("user_id",userId);
        Address address = addressMapper.selectOne(queryWrapper);
        if(address == null)
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(), ResponseCode.ARGUMENT_ILLEGAL.getDescription());

        addressMapper.delete(queryWrapper);
        return CommonResponse.createForSuccess("SUCCESS");
    }

    @Override
    public CommonResponse<AddressVO> updateAddress(UpdateAddressDTO updateAddressDTO) {
        QueryWrapper<Address> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("id",updateAddressDTO.getId()).eq("user_id",updateAddressDTO.getUserId());
        Address address = addressMapper.selectOne(queryWrapper);
        if(address == null)
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(), ResponseCode.ARGUMENT_ILLEGAL.getDescription());

        Address address1 = new Address();
        UpdateWrapper<Address> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", updateAddressDTO.getId())
                .eq("user_id",updateAddressDTO.getUserId())
                .set("address_name",updateAddressDTO.getAddressName())
                .set("address_phone",updateAddressDTO.getAddressPhone())
                .set("address_mobile",updateAddressDTO.getAddressMobile())
                .set("address_province",updateAddressDTO.getAddressProvince())
                .set("address_city",updateAddressDTO.getAddressCity())
                .set("address_district",updateAddressDTO.getAddressDistrict())
                .set("address_detail",updateAddressDTO.getAddressDetail())
                .set("address_zip",updateAddressDTO.getAddressZip())
                .set("update_time",LocalDateTime.now());

        int rows = addressMapper.update(address1, updateWrapper);
        if (rows == 0) {
            return CommonResponse.createForError("更新数据库失败");
        }

        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(updateAddressDTO, addressVO);
        return CommonResponse.createForSuccess(addressVO);
    }

    @Override
    public CommonResponse<AddressVO> findAddress(Integer addressId) {
        Address address = addressMapper.selectById(addressId);
        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(address, addressVO);
        return CommonResponse.createForSuccess(addressVO);
    }

    @Override
    public CommonResponse<List<AddressVO>> listAddress(Integer userId) {
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Address> addressList = addressMapper.selectList(queryWrapper);
        List<AddressVO> addressVOList = ListBeanUtils.copyListProperties(addressList, AddressVO::new);
        return CommonResponse.createForSuccess(addressVOList);
    }

    @Override
    public CommonResponse<List<AddressVO>> list(Integer userId) {
        List<AddressVO> addressVOList = new ArrayList<>();
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        if(userId != null){
            queryWrapper.eq("user_id",userId);
        }
        List<Address> addressList = addressMapper.selectList(queryWrapper);
        for(Address address:addressList){
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(address,addressVO);
            addressVOList.add(addressVO);
        }
        return CommonResponse.createForSuccess(addressVOList);
    }

    @Override
    public CommonResponse<String> delete(Integer addressId) {
        int result = addressMapper.deleteById(addressId);
        if (result != 0)
            return CommonResponse.createForSuccessMessage("删除成功");
        else
            return CommonResponse.createForError("删除失败");
    }
}

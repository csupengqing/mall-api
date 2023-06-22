package org.csu.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mystore_address")
public class Address {
    private Integer id;
    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "address_name")
    @NotBlank(message = "地址名称不能为空")
    private String addressName;

    @TableField(value = "address_phone")
    @NotBlank(message = "地址电话不能为空")
    private String addressPhone;

    @TableField(value = "address_mobile")
    @NotBlank(message = "移动电话不能为空")
    private String addressMobile;

    @TableField(value = "address_province")
    @NotBlank(message = "地址省份不能为空")
    private String addressProvince;

    @TableField(value = "address_city")
    @NotBlank(message = "地址城市不能为空")
    private String addressCity;

    @TableField(value = "address_district")
    @NotBlank(message = "地址区县不能为空")
    private String addressDistrict;

    @TableField(value = "address_detail")
    @NotBlank(message = "地址详情不能为空")
    private String addressDetail;

    @TableField(value = "address_zip")
    @NotBlank(message = "地址邮编不能为空")
    private String addressZip;

    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}

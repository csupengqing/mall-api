package org.csu.api.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mystore_address")
public class Adress {
    private Integer id;
//    @TableField("user_id")
    private Integer userId;
    @NotBlank(message = "地址名称不能为空")
    private String addressName;
    @NotBlank(message = "地址电话不能为空")
    private String addressPhone;
    @NotBlank(message = "移动电话不能为空")
    private String addressMobile;
    @NotBlank(message = "地址省份不能为空")
    private String addressProvince;
    @NotBlank(message = "地址城市不能为空")
    private String addressCity;
    @NotBlank(message = "地址区县不能为空")
    private String addressDistrict;
    @NotBlank(message = "地址详情不能为空")
    private String addressDetail;
    @NotBlank(message = "地址邮编不能为空")
    private String addressZip;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

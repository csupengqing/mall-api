package org.csu.api.vo;

import lombok.Data;

@Data
public class AdressVO {
    private Integer id;
    private Integer userId;
    private String addressName;
    private String addressPhone;
    private String addressMobile;
    private String addressProvince;
    private String addressCity;
    private String addressDistrict;
    private String addressDetail;
    private String addressZip;
}

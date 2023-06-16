package org.csu.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@TableName("mystore_order")
public class Order {
    private Integer ID;
    private BigInteger orderNo;
    @TableField("user_id")
    private Integer userId;
    @TableField("address_id")
    private Integer addressId;
    private BigDecimal paymentPrice;
    private Integer paymentType;
    private Integer postage;
    private Integer status;
    private LocalDateTime paymentTime;
    private LocalDateTime sendTime;
    private LocalDateTime endTime;
    private LocalDateTime closeTime;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}

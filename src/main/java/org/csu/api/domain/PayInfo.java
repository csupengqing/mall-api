package org.csu.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@TableName("mystore_pay_info")
public class PayInfo {
    private Integer id;
    @TableField(value = "user_id")
    private Integer userId;
    @TableField(value = "order_no")
    private BigInteger orderNo;
    @TableField(value = "payment_type")
    private Integer paymentType;
    @TableField(value = "trade_no")
    private String tradeNo;
    @TableField(value = "trade_status")
    private String tradeStatus;
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}

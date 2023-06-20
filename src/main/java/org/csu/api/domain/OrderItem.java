package org.csu.api.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@TableName("mystore_order_item")
public class OrderItem {
    private Integer ID;
    @TableField("user_id")
    private Integer userId;
    @TableField("product_id")
    private Integer productId;
    private BigInteger orderNo;
    private String productName;
    private String productImage;
    private BigDecimal currentPrice;

    private Integer quantity;
    private BigDecimal totalPrice;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}

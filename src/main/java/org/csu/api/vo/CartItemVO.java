package org.csu.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemVO {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private Integer checked;

    private String productName;
    private String productSubtitle;
    private BigDecimal productPrice;
    private Integer productStock;
    private String productMainImage;
    private BigDecimal productTotalPrice;
    private Boolean checkStock;
}

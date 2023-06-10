package org.csu.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInfoDTO {
    @NotNull(message = "类别ID不能为空")
    private Integer categoryId;
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;
    private String mainImage;
    private String subImages;
    private String detail;
    @NotNull(message = "商品价格不能为空")
    private BigDecimal price;
    @NotNull(message = "商品库存不能为空")
    private Integer stock;
    @NotNull(message = "商品状态不能为空")
    private Integer status;
}

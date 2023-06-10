package org.csu.api.controller.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.dto.ProductInfoDTO;
import org.csu.api.service.ProductService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product")
@Validated
public class AdminProductController {
    @Autowired
    private ProductService productService;

    //新增商品
    @PostMapping("/add_product")
    public CommonResponse<String> addProduct(@Valid @RequestBody ProductInfoDTO productInfoDTO,
                                         HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return productService.addProduct(productInfoDTO);
    }

    //删除商品
    @PostMapping("/delete_product")
    public CommonResponse<String> deleteProduct(@RequestParam @NotNull(message = "商品ID不能为空") Integer productId,
                                                HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return productService.deleteProduct(productId);
    }

    //修改商品信息
    @PostMapping("/update_product_info")
    public CommonResponse<String> updateProductInfo(@NotNull(message = "商品ID不能为空") Integer productId,
                                                    @Valid @RequestBody ProductInfoDTO productInfoDTO,
                                                HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return productService.updateProductInfo(productId,productInfoDTO);
    }
}

package org.csu.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.api.common.CommonResponse;
import org.csu.api.dto.ProductInfoDTO;
import org.csu.api.dto.UpdateProductDTO;
import org.csu.api.vo.ProductDetailVO;
import org.csu.api.vo.ProductListVO;

import java.util.List;

public interface ProductService {

    //根据商品ID查询商品的详情
    CommonResponse<ProductDetailVO> getProductDetail(Integer productId);

    //查询商品列表
    CommonResponse<Page<ProductListVO>> getProductList(Integer categoryId, String keyword, String orderBy, int pageNum, int pageSize);

    //添加商品————管理员权限
    CommonResponse<String> addProduct(ProductInfoDTO productInfoDTO);

    //删除商品————管理员权限
    CommonResponse<String> deleteProduct(String productIds);

    //修改商品信息————管理员权限
    CommonResponse<String> updateProductInfo(UpdateProductDTO updateProductDTO);
    //查看所有商品————管理员权限
    CommonResponse<List<ProductDetailVO>> list(String keyword);
}

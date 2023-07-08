package org.csu.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.domain.Category;
import org.csu.api.domain.Product;
import org.csu.api.dto.ProductInfoDTO;
import org.csu.api.dto.UpdateProductDTO;
import org.csu.api.persistence.CategoryMapper;
import org.csu.api.persistence.ProductMapper;
import org.csu.api.service.CategoryService;
import org.csu.api.service.ProductService;
import org.csu.api.util.ImageServerConfig;
import org.csu.api.util.ListBeanUtilsForPage;
import org.csu.api.vo.ProductDetailVO;
import org.csu.api.vo.ProductListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("productService")
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryService categoryService;

    @Resource
    private ImageServerConfig imageServerConfig;

    @Override
    public CommonResponse<ProductDetailVO> getProductDetail(Integer productId) {
        if(productId == null){
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        }

        Product product = productMapper.selectById(productId);

        if(product == null){
            return CommonResponse.createForError("产品不存在或已删除");
        }
        //此处判断商品是否处于在售状态，如果未以后后台管理系统要复用代码的话，要加以区分
        if(product.getStatus() != CONSTANT.ProductStatus.ON_SALE.getCode()){
            return CommonResponse.createForError("产品不在售，下架或其他情况");
        }

        ProductDetailVO productDetailVO = productToProductDetailVO(product);
        return CommonResponse.createForSuccess(productDetailVO);
    }

    @Override
    public CommonResponse<Page<ProductListVO>> getProductList(Integer categoryId, String keyword, String orderBy, int pageNum, int pageSize) {
        //客户端提交的keyword和分类ID都为空
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        }

        if(categoryId != null){
            Category category = categoryMapper.selectById(categoryId);
            //查不到前端传来的Category信息，并且keyword也为空，则表明数据库中没有该分类，关键字也为空，则返回空的结果集，打日志
            if(category == null && StringUtils.isBlank(keyword)){
                log.info("没有查到分类ID为{}的商品信息", categoryId);
                return CommonResponse.createForSuccessMessage("没有查到商品信息");
            }
        }

        Page<Product> result = new Page<>();
        result.setCurrent(pageNum);
        result.setSize(pageSize);

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

        //查询条件中增加分类查询
        List<Integer> categoryIdList = categoryService.getCategoryAndAllChildren(categoryId).getData();
        if(categoryIdList.size() != 0){
            queryWrapper.in("category_id", categoryIdList);
        }

        //查询条件中增加按分类名称模糊查询
        if(StringUtils.isNotBlank(keyword)){
            //keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
            queryWrapper.like("name", "%"+keyword+"%");
        }

        //查询条件中增加排序条件
        if(StringUtils.isNotBlank(orderBy)){
            if(StringUtils.equals(orderBy, CONSTANT.PRODUCT_ORDER_BY_PRICE_ASC)){
                queryWrapper.orderByAsc("price");
            }else if(StringUtils.equals(orderBy, CONSTANT.PRODUCT_ORDER_BY_PRICE_DESC)){
                queryWrapper.orderByDesc("price");
            }
        }

        result = productMapper.selectPage(result, queryWrapper);
//        List<Product> productList = result.getRecords();
//        List<ProductListVO> productListVOList = ListBeanUtils.copyListProperties(productList, ProductListVO::new, (product, productListVO) -> {
//            productListVO.setImageServer(imageServerConfig.getUrl());
//        } );

        Page<ProductListVO> result1 = ListBeanUtilsForPage.copyPageList(result, ProductListVO::new, (product, productListVO) -> {
            productListVO.setImageServer(imageServerConfig.getUrl());
        } );

        return CommonResponse.createForSuccess(result1);
    }

    private ProductDetailVO productToProductDetailVO(Product product){
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(product, productDetailVO);

        Category category = categoryMapper.selectById(product.getCategoryId());
        productDetailVO.setParentCategoryId(category.getParentId());

        productDetailVO.setImageServer(imageServerConfig.getUrl());
        return productDetailVO;
    }

    @Override
    public CommonResponse<String> addProduct(ProductInfoDTO productInfoDTO) {
        Category category = categoryMapper.selectById(productInfoDTO.getCategoryId());
        if(category == null){
            return CommonResponse.createForError("没有要添加至的商品类别");
        }
        else{
            Product product = new Product();
            BeanUtils.copyProperties(productInfoDTO,product);
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());

            int rows = productMapper.insert(product);
            if(rows == 0){
                return CommonResponse.createForError("添加商品失败");
            }
            return CommonResponse.createForSuccessMessage("添加商品成功");
        }
    }

    @Override
    public CommonResponse<String> deleteProduct(String productIds) {
        String[] idList = productIds.split(",");
        int result = 0;
        for(String id:idList){
            Product product = productMapper.selectById(id);
            if(product == null){
                return CommonResponse.createForError("商品ID不存在");
            }
            else{
                UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id",Integer.parseInt(id))
                        .set("status",CONSTANT.ProductStatus.DELETE.getCode());
                result += productMapper.update(product,updateWrapper);
        }
        }
        if(result != idList.length){
            return CommonResponse.createForError("删除商品失败");
        }
        else{
            return CommonResponse.createForSuccessMessage("删除商品成功");
        }
    }

    @Override
    public CommonResponse<String> updateProductInfo(UpdateProductDTO updateProductDTO) {
        Product product = productMapper.selectById(updateProductDTO.getId());
        if(product == null){
            return CommonResponse.createForError("该商品不存在");
        }
        else{
            if(categoryMapper.selectById(updateProductDTO.getCategoryId()) == null){
                return CommonResponse.createForError("要修改至的类别不存在");
            }
            else{
                Product product1 = new Product();
                UpdateWrapper<Product> updateWrapper =new UpdateWrapper<>();
                updateWrapper.eq("id",updateProductDTO.getId());
                updateWrapper.set("category_id",updateProductDTO.getCategoryId())
                        .set("name",updateProductDTO.getName())
                        .set("subtitle",updateProductDTO.getSubtitle())
                        .set("main_image",updateProductDTO.getMainImage())
                        .set("sub_images",updateProductDTO.getSubImages())
                        .set("detail",updateProductDTO.getDetail())
                        .set("price",updateProductDTO.getPrice())
                        .set("stock",updateProductDTO.getStock())
                        .set("status",updateProductDTO.getStatus())
                        .set("update_time",LocalDateTime.now());
                int rows = productMapper.update(product1, updateWrapper);
                if (rows > 0) {
                    return CommonResponse.createForSuccess("SUCCESS");
                }
                return CommonResponse.createForError("修改商品信息失败");
            }
        }
    }

    @Override
    public CommonResponse<List<ProductDetailVO>> list(String keyword) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(keyword)){
            queryWrapper.like("name", "%"+keyword+"%");
        }
        List<Product> productList = productMapper.selectList(queryWrapper);
        List<ProductDetailVO> productDetailVOList = new ArrayList<>();
        for(Product product:productList){
              ProductDetailVO productDetailVO = new ProductDetailVO();
              BeanUtils.copyProperties(product,productDetailVO);
              productDetailVOList.add(productDetailVO);
        }
        return CommonResponse.createForSuccess(productDetailVOList);
    }
}

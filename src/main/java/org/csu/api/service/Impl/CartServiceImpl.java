package org.csu.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.domain.CartItem;
import org.csu.api.domain.Product;
import org.csu.api.persistence.CartItemMapper;
import org.csu.api.persistence.ProductMapper;
import org.csu.api.service.CartService;
import org.csu.api.util.BigDecimalUtil;
import org.csu.api.util.ImageServerConfig;
import org.csu.api.util.ListBeanUtils;
import org.csu.api.vo.CartItemVO;
import org.csu.api.vo.CartVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service("cartService")
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Resource
    private ImageServerConfig imageServerConfig;

    @Override
    public CommonResponse<CartVO> addCart(Integer userId, Integer productId, Integer quantity) {
        //productId 校验
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",productId).eq("status", CONSTANT.ProductStatus.ON_SALE.getCode());
        Product product =productMapper.selectOne(queryWrapper);
        if(product == null){
            return CommonResponse.createForError("商品ID不存在或已下架");
        }
        //新增或数量增加
        QueryWrapper<CartItem> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("user_id",userId).eq("product_id",productId);
        CartItem cartItem = cartItemMapper.selectOne(queryWrapper2);

        if(cartItem == null){
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItem.setChecked(CONSTANT.CART_ITEM_STATUS.CHECKED);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(cartItem);
        }
        else {
            UpdateWrapper<CartItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",cartItem.getId());
            updateWrapper.set("quantity",quantity);
            updateWrapper.set("update_time",LocalDateTime.now());
            cartItemMapper.update(cartItem,updateWrapper);
        }
        //返回
        return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
    }

    @Override
    public CommonResponse<CartVO> updateCart(Integer userId, Integer productId, Integer quantity) {
        //productId 校验
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",productId).eq("status", CONSTANT.ProductStatus.ON_SALE.getCode());
        Product product =productMapper.selectOne(queryWrapper);
        if(product == null){
            return CommonResponse.createForError("商品ID不存在或已下架");
        }
        //更新数量
        QueryWrapper<CartItem> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("user_id",userId).eq("product_id",productId);
        CartItem cartItem = cartItemMapper.selectOne(queryWrapper2);
        if(cartItem != null){
            UpdateWrapper<CartItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id",cartItem.getId());
            updateWrapper.set("quantity",quantity);
            updateWrapper.set("update_time",LocalDateTime.now());
            cartItemMapper.update(cartItem,updateWrapper);
            //返回
            return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
        }
        else{
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        }
    }

    @Override
    public CommonResponse<CartVO> deleteCart(Integer userId, String productIds) {
        String[] productIdList = productIds.split(",");
        if(productIdList.length != 0){
            //删除
            for(String productId : productIdList){
                //查询是否存在
                QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id",userId).eq("product_id",Integer.parseInt(productId));
                CartItem cartItem = cartItemMapper.selectOne(queryWrapper);
                //不存在
                if(cartItem == null){
                    return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
                }
                else{
                    cartItemMapper.deleteById(cartItem.getId());
                }
            }
        }
        else
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        //返回删除后的购物车数据
        return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
    }

    @Override
    public CommonResponse<CartVO> list(Integer userId) {
        return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
    }

    @Override
    public CommonResponse<CartVO> setAllChecked(Integer userId) {
        //查
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartItem> cartItemList = cartItemMapper.selectList(queryWrapper);
        //全选
        if(CollectionUtils.isNotEmpty(cartItemList)){
            for(CartItem cartItem : cartItemList){
                CartItem updateChecked = new CartItem();
                UpdateWrapper<CartItem> cartItemUpdateWrapper = new UpdateWrapper<>();
                cartItemUpdateWrapper.eq("user_id",userId).eq("product_id",cartItem.getProductId());
                cartItemUpdateWrapper.set("checked",CONSTANT.CART_ITEM_STATUS.CHECKED);
                cartItemMapper.update(updateChecked,cartItemUpdateWrapper);
            }
        }
        return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
    }

    @Override
    public CommonResponse<CartVO> setAllUnchecked(Integer userId) {
        //查
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartItem> cartItemList = cartItemMapper.selectList(queryWrapper);
        //全不选
        if(CollectionUtils.isNotEmpty(cartItemList)){
            for(CartItem cartItem : cartItemList){
                CartItem updateChecked = new CartItem();
                UpdateWrapper<CartItem> cartItemUpdateWrapper = new UpdateWrapper<>();
                cartItemUpdateWrapper.eq("user_id",userId).eq("product_id",cartItem.getProductId());
                cartItemUpdateWrapper.set("checked",CONSTANT.CART_ITEM_STATUS.UNCHECKED);
                cartItemMapper.update(updateChecked,cartItemUpdateWrapper);
            }
        }
        return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
    }

    @Override
    public CommonResponse<CartVO> setCartItemChecked(Integer userId, Integer productId) {
        //查
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId).eq("product_id",productId);
        CartItem cartItem = cartItemMapper.selectOne(queryWrapper);

        if(cartItem == null)
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        else{
            CartItem updateChecked = new CartItem();
            UpdateWrapper<CartItem> cartItemUpdateWrapper = new UpdateWrapper<>();
            cartItemUpdateWrapper.eq("user_id",userId).eq("product_id",productId);
            cartItemUpdateWrapper.set("checked",CONSTANT.CART_ITEM_STATUS.CHECKED);
            cartItemMapper.update(updateChecked,cartItemUpdateWrapper);

            return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
        }
    }

    @Override
    public CommonResponse<CartVO> setCartItemUnchecked(Integer userId, Integer productId) {
        //查
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId).eq("product_id",productId);
        CartItem cartItem = cartItemMapper.selectOne(queryWrapper);

        if(cartItem == null)
            return CommonResponse.createForError(ResponseCode.ARGUMENT_ILLEGAL.getCode(),ResponseCode.ARGUMENT_ILLEGAL.getDescription());
        else{
            CartItem updateChecked = new CartItem();
            UpdateWrapper<CartItem> cartItemUpdateWrapper = new UpdateWrapper<>();
            cartItemUpdateWrapper.eq("user_id",userId).eq("product_id",productId);
            cartItemUpdateWrapper.set("checked",CONSTANT.CART_ITEM_STATUS.UNCHECKED);
            cartItemMapper.update(updateChecked,cartItemUpdateWrapper);

            return CommonResponse.createForSuccess(this.getCartVOAndCheckStock(userId));
        }
    }

    @Override
    public CommonResponse<Integer> getCartCount(Integer userId) {
        //查
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartItem> cartItemList = cartItemMapper.selectList(queryWrapper);

        return CommonResponse.createForSuccess("SUCCESS",cartItemList.size());
    }

    private CartVO getCartVOAndCheckStock(Integer userId){
        CartVO cartVO = new CartVO();
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartItem> cartItemList = cartItemMapper.selectList(queryWrapper);

        List<CartItemVO> cartItemVOList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        Boolean allSelected = true;

        if(CollectionUtils.isNotEmpty(cartItemList)){
            //工具类实现
//            cartItemVOList = ListBeanUtils.copyListProperties(cartItemList,CartItemVO::new,(cartItem,cartItemVO)->{
//                Product product = productMapper.selectById(cartItem.getProductId());
//                //商品存在
//                if(product != null){
//                    BeanUtils.copyProperties(product,cartItemVO);
//                    if(product.getStock() >= cartItem.getQuantity()){
//                        cartItemVO.setQuantity(cartItem.getQuantity());
//                        cartItemVO.setCheckStock(true);
//                    }
//                    else {
//                        cartItemVO.setQuantity(product.getStock());
//                        CartItem updateStockCart = new CartItem();
//                        UpdateWrapper<CartItem> updateWrapper = new UpdateWrapper<>();
//                        updateWrapper.eq("id",cartItem.getId());
//                        updateWrapper.set("quantity",product.getStock());
//                        cartItemMapper.update(updateStockCart,updateWrapper);
//                        cartItemVO.setCheckStock(false);
//                    }
//                    cartItemVO.setProductTotalPrice(BigDecimalUtil.multiply(cartItemVO.getQuantity(),cartItemVO.getProductPrice().doubleValue()));
//                }
//
//                if(cartItem.getChecked() == CONSTANT.CART_ITEM_STATUS.CHECKED){
//                    cartTotalPrice.set(BigDecimalUtil.add(cartTotalPrice.get().doubleValue(), cartItemVO.getProductTotalPrice().doubleValue()));
//                }else{
//                    allSelected.set(false);
//                }
//            });
//
//            cartVO.setCartItemVOList(cartItemVOList);
//            cartVO.setAllSelected(allSelected.get());
//            cartVO.setCartTotalPrice(cartTotalPrice.get());
//            cartVO.setProductImageServer("CSU");

            //普通实现
            for(CartItem cartItem : cartItemList){
                CartItemVO cartItemVO = new CartItemVO();

                cartItemVO.setId(cartItem.getId());
                cartItemVO.setUserId(cartItem.getUserId());
                cartItemVO.setProductId(cartItem.getProductId());
                cartItemVO.setChecked(cartItem.getChecked());

                Product product = productMapper.selectById(cartItem.getProductId());
                if(product != null){
                    cartItemVO.setProductName(product.getName());
                    cartItemVO.setProductSubtitle(product.getSubtitle());
                    cartItemVO.setProductMainImage(product.getMainImage());
                    cartItemVO.setProductPrice(product.getPrice());
                    cartItemVO.setProductStock(product.getStock());

                    if(product.getStock() >= cartItem.getQuantity())
                    {
                        cartItemVO.setQuantity(cartItem.getQuantity());
                        cartItemVO.setCheckStock(true);
                    }
                    else
                    {
                        cartItemVO.setQuantity(product.getStock());
                        CartItem updateStockCart = new CartItem();
                        UpdateWrapper<CartItem> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("id",cartItem.getId());
                        updateWrapper.set("quantity",product.getStock());
                        cartItemMapper.update(updateStockCart,updateWrapper);
                        cartItemVO.setCheckStock(false);
                    }
                    cartItemVO.setProductTotalPrice(BigDecimalUtil.multiply(cartItemVO.getProductPrice().doubleValue(),cartItemVO.getQuantity().doubleValue()));
                }
                cartItemVOList.add(cartItemVO);
                if(cartItem.getChecked() == CONSTANT.CART_ITEM_STATUS.CHECKED){
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartItemVO.getProductTotalPrice().doubleValue());
                }
                else{
                    allSelected = false;
                }
            }

            cartVO.setCartItemVOList(cartItemVOList);
            cartVO.setAllSelected(allSelected);
            cartVO.setCartTotalPrice(cartTotalPrice);
            cartVO.setProductImageServer(imageServerConfig.getUrl());
            return cartVO;
        }
        return cartVO;
    }
}

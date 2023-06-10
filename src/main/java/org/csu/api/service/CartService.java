package org.csu.api.service;

import org.csu.api.common.CommonResponse;
import org.csu.api.vo.CartVO;

import java.util.List;

public interface CartService {
    //添加购物车
    CommonResponse<CartVO> addCart(Integer userId,Integer ProductId,Integer quantity);
    //更新购物车
    CommonResponse<CartVO> updateCart(Integer userId,Integer productId,Integer quantity);
    //删除购物车项
    CommonResponse<CartVO> deleteCart(Integer userId, String productIds);
    //获取购物车列表
    CommonResponse<CartVO> list(Integer userId);
    //购物车全选
    CommonResponse<CartVO> setAllChecked(Integer userId);
    //购物车全不选
    CommonResponse<CartVO> setAllUnchecked(Integer userId);
    //选择某项商品
    CommonResponse<CartVO> setCartItemChecked(Integer userId,Integer productId);
    //取消选择某项商品
    CommonResponse<CartVO> setCartItemUnchecked(Integer userId,Integer productId);
    //获取购物车总数
    CommonResponse<Integer> getCartCount(Integer userId);
}

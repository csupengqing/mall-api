package org.csu.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.api.common.CommonResponse;
import org.csu.api.vo.OrderVO;

import java.math.BigInteger;

public interface OrderService {
    CommonResponse<Object> createOrder(Integer userId, Integer addressId);
    CommonResponse<Object> getCartItemList(Integer userId);
    CommonResponse<OrderVO> getDetail(Integer userId, BigInteger orderNo);
    CommonResponse<Page<OrderVO>> getList(Integer userId, int pageNum, int pageSize);
    CommonResponse<String> cancel(Integer userId, BigInteger orderNo);
}

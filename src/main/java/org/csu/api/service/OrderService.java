package org.csu.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csu.api.common.CommonResponse;
import org.csu.api.vo.OrderVO;

import java.awt.color.CMMException;
import java.math.BigInteger;
import java.util.List;

public interface OrderService {
    CommonResponse<Object> createOrder(Integer userId, Integer addressId);
    CommonResponse<Object> getCartItemList(Integer userId);
    CommonResponse<OrderVO> getDetail(Integer userId, BigInteger orderNo);
    CommonResponse<Page<OrderVO>> getList(Integer userId, int pageNum, int pageSize);
    CommonResponse<String> cancel(Integer orderId);
    //管理员查看所有订单
    CommonResponse<Page<OrderVO>> list(Integer orderStatus, String orderBy, int pageNum, int pageSize);
    CommonResponse<String> send(Integer orderId);
    CommonResponse<String> success(Integer orderId);
    CommonResponse<String> close(Integer orderId);
}

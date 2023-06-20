package org.csu.api.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.domain.Adress;
import org.csu.api.domain.Order;
import org.csu.api.domain.OrderItem;
import org.csu.api.domain.Product;
import org.csu.api.persistence.*;
import org.csu.api.service.CartService;
import org.csu.api.service.OrderService;
import org.csu.api.util.ListBeanUtils;
import org.csu.api.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private AdressMapper adressMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartService cartService;

    @Override
    public CommonResponse<Object> createOrder(Integer userId, Integer addressId) {
        // 判断地址是否存在，生成addressVO
        Adress adress = adressMapper.selectById(addressId);
        if(adress == null) {
            return CommonResponse.createForError("地址不存在");
        }
        AdressVO adressVO = new AdressVO();
        BeanUtils.copyProperties(adress, adressVO);

        // 生成orderNo，随机且支持索引（不知道支持索引是啥意思，存在数据库里算支持索引吗？）
        // 获取时间戳（从1970年1月1日UTC开始的秒数）+ 10位随机数
        Instant instant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        long timeStampSeconds = instant.getEpochSecond();
        String randomNumeric = RandomStringUtils.randomNumeric(9);
        BigInteger orderNo = new BigInteger(String.valueOf(timeStampSeconds) + randomNumeric);

        // 通过userId查询购物车，先生成订单条目，再生成订单列表，通过订单列表的价格计算总价格
        CartVO cartVO = (CartVO)cartService.list(userId).getData();
        List<CartItemVO> cartItemVOList = cartVO.getCartItemVOList();

        List<OrderItemVO> orderItemVOList = Lists.newArrayList();

        if(CollectionUtils.isEmpty(cartItemVOList)) {
            return CommonResponse.createForSuccess("创建订单失败，请先添加购物车");
        }

        // 计算购物车中选中的项是否为0
        int checked = 0;
        for(CartItemVO cartItemVO : cartItemVOList) {
            if(cartItemVO.getChecked() == 1){
                // 生成订单同时删除购物车相应项、修改库存
                OrderItem orderItem = new OrderItem();
                orderItem.setUserId(userId);
                orderItem.setProductId(cartItemVO.getProductId());
                orderItem.setOrderNo(orderNo);
                orderItem.setProductName(cartItemVO.getProductName());
                orderItem.setProductImage(cartItemVO.getProductMainImage());
                orderItem.setCurrentPrice(cartItemVO.getProductPrice());
                orderItem.setQuantity(cartItemVO.getQuantity());
                orderItem.setTotalPrice(cartItemVO.getProductTotalPrice());
                orderItem.setCreateTime(LocalDateTime.now());
                orderItem.setUpdateTime(LocalDateTime.now());
                orderItemMapper.insert(orderItem);
                cartService.deleteCart(userId, String.valueOf(cartItemVO.getProductId()));
                Product product = productMapper.selectById(cartItemVO.getProductId());
                UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", cartItemVO.getProductId());
                updateWrapper.set("stock", Math.max(product.getStock() - cartItemVO.getQuantity(), 0));
                productMapper.update(product, updateWrapper);

                OrderItemVO orderItemVO = new OrderItemVO();
                BeanUtils.copyProperties(orderItem, orderItemVO);
                orderItemVOList.add(orderItemVO);

                checked++;
            }
        }

        if(checked == 0) {
            return CommonResponse.createForSuccess("创建订单失败，请先选中商品");
        }

        // 创建Order，插入数据库
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setPaymentPrice(cartVO.getCartTotalPrice());
        order.setPaymentType(CONSTANT.PAYMENT_TYPE.ZHI_FU_BAO);
        order.setPostage(0);
        order.setStatus(CONSTANT.ORDER_STATUS.UN_PAID);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 创建OrderVO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setAdressVO(adressVO);
        orderVO.setOrderItemVOList(orderItemVOList);
        return CommonResponse.createForSuccess(orderVO);
    }

    @Override
    public CommonResponse<Object> getCartItemList(Integer userId) {
        // 通过userId查询购物车
        CartVO cartVO = (CartVO)cartService.list(userId).getData();
        List<CartItemVO> cartItemVOList = cartVO.getCartItemVOList();

        List<OrderItemVO> orderItemVOList = Lists.newArrayList();

        if(CollectionUtils.isEmpty(cartItemVOList)) {
            return CommonResponse.createForError("购物车为空");
        }

        for(CartItemVO cartItemVO : cartItemVOList) {
            if(cartItemVO.getChecked() == 1){
                OrderItemVO orderItemVO = new OrderItemVO();
                orderItemVO.setProductId(cartItemVO.getProductId());
                orderItemVO.setProductName(cartItemVO.getProductName());
                orderItemVO.setProductImage(cartItemVO.getProductMainImage());
                orderItemVO.setCurrentPrice(cartItemVO.getProductPrice());
                orderItemVO.setQuantity(cartItemVO.getQuantity());
                orderItemVO.setTotalPrice(cartItemVO.getProductTotalPrice());

                orderItemVOList.add(orderItemVO);
            }
        }
        return CommonResponse.createForSuccess(orderItemVOList);
    }

    @Override
    public CommonResponse<OrderVO> getDetail(Integer userId, BigInteger orderNo) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("order_no", orderNo);
        Order order = orderMapper.selectOne(queryWrapper);
        if (order == null) {
            return CommonResponse.createForError("订单不存在");
        }

        QueryWrapper<OrderItem> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("user_id", userId).eq("order_no", orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectList(queryWrapper2);
        List<OrderItemVO> orderItemVOList = ListBeanUtils.copyListProperties(orderItemList, OrderItemVO::new);

        Adress adress = adressMapper.selectById(order.getAddressId());
        AdressVO adressVO = new AdressVO();
        BeanUtils.copyProperties(adress, adressVO);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderItemVOList(orderItemVOList);
        orderVO.setAdressVO(adressVO);

        return CommonResponse.createForSuccess(orderVO);
    }

    @Override
    public CommonResponse<Page<OrderVO>> getList(Integer userId, int pageNum, int pageSize) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Order> orderList = orderMapper.selectList(queryWrapper);
        List<OrderVO> orderVOList = Lists.newArrayList();
        for(Order order:orderList) {
            OrderVO orderVO = this.getDetail(userId, order.getOrderNo()).getData();
            orderVOList.add(orderVO);
        }

        Page<OrderVO> result = new Page<>();
        result.setRecords(orderVOList);
        result.setCurrent(pageNum);
        result.setSize(pageSize);
        result.setTotal(result.getRecords().size());
        return CommonResponse.createForSuccess(result);
    }

    @Override
    public CommonResponse<String> cancel(Integer userId, BigInteger orderNo) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("order_no", orderNo);
        Order order = orderMapper.selectOne(queryWrapper);

        QueryWrapper<OrderItem> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("user_id", userId).eq("order_no", orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectList(queryWrapper2);
        if (order == null) {
            return CommonResponse.createForError("订单不存在");
        } else if (order.getStatus() != CONSTANT.ORDER_STATUS.UN_PAID) {
            return CommonResponse.createForError("订单不是为支付状态，不能取消");
        }

        int rows = orderMapper.delete(queryWrapper);
        int rows2 = orderItemMapper.delete(queryWrapper2);
        if (rows == 0 || rows2 == 0) {
            return CommonResponse.createForError("取消订单失败");
        }
        return CommonResponse.createForSuccess("SUCCESS");
    }
}

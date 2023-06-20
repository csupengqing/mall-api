package org.csu.api.service.Impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import org.csu.api.vo.OrderVO;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.domain.Order;
import org.csu.api.persistence.OrderMapper;
import org.csu.api.service.AlipayService;
import org.csu.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@Slf4j
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Value("${alipay.appId}")
    private String appId;

    @Value("${alipay.gateway}")
    private String gateway;

    @Value("${alipay.appPrivateKey}")
    private String appPrivateKey;

    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;

    @Override
    public String pay(BigInteger orderNo) throws AlipayApiException {
        // 实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gateway, appId, appPrivateKey, "json", "utf-8", alipayPublicKey, "RSA2");
        // 实例化请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        // 设置异步通知地址（注意这里部署的时候要换上公网地址，目前是使用netapp进行了内网穿透）
        alipayRequest.setNotifyUrl("http://depzhm.natappfree.cc/alipay/notify_url");
        // 设置同步通知地址，跳转到到订单详情页面，这里还没测试
        // 要注意，同步通知返回的支付成功仅说明接口调用成功，这里仅改变前端数据，不修改数据库数据，等异步请求验证成功后再修改数据库数据
        alipayRequest.setReturnUrl("http://localhost:8090/order/detail?orderNo=" + orderNo);
        // 设置订单总金额
        QueryWrapper<Order> queryWrapper = new QueryWrapper<Order>();
        queryWrapper.eq("order_no", orderNo);
        Order order = orderMapper.selectOne(queryWrapper);
        String totalAmount = order.getPaymentPrice().toString();
        // 设置订单标题
        String subject = "电脑网站支付";
        // 组装请求参数
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        String outTradeNo = String.valueOf(orderNo);
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(totalAmount);
        model.setSubject(subject);
        model.setBody("电脑网站支付");
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizModel(model);
        // 请求支付宝进行支付
        String form = alipayClient.pageExecute(alipayRequest).getBody();
        return form;
    }

    @Override
    public CommonResponse<Object> returnUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }
        // 验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        if (signVerified) {
            // 获取订单号
            String outTradeNo = params.get("out_trade_no");
            // 获取支付宝交易号
            String tradeNo = params.get("trade_no");
            // 获取交易状态
            AlipayClient alipayClient = new DefaultAlipayClient(gateway, appId, appPrivateKey, "json", "utf-8", alipayPublicKey, "RSA2");
            AlipayTradeQueryRequest request1 = new AlipayTradeQueryRequest();
            request1.setBizContent("{" +
                    "  \"out_trade_no\":\"" + outTradeNo + "\"," +
                    "  \"trade_no\":\"" + tradeNo + "\"," +
                    "  \"query_options\":[" +
                    "    \"trade_settle_info\"" +
                    "  ]" +
                    "}");
            AlipayTradeQueryResponse response = alipayClient.execute(request1);
            if(response.isSuccess()){
                QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_no", new BigInteger(outTradeNo));
                Order order = orderMapper.selectOne(queryWrapper);
                Integer userId = order.getUserId();
                OrderVO orderVO = orderService.getDetail(userId, new BigInteger(outTradeNo)).getData();
                orderVO.setStatus(CONSTANT.ORDER_STATUS.PAID);
                orderVO.setEndTime(LocalDateTime.now());
                return CommonResponse.createForSuccess("支付成功", orderVO);
            }
        }
        return CommonResponse.createForError("支付失败");
    }

    @Override
    public CommonResponse<String> notifyUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }
        String outTradeNo = params.get("out_trade_no");
        // 验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        if (signVerified) {
            // 获取交易状态
            String tradeStatus = params.get("trade_status");
            if (tradeStatus.equals("TRADE_SUCCESS")) {
                // 修改订单状态、交易完成时间
                Order order = new Order();
                UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("order_no", new BigInteger(outTradeNo));
                order.setOrderNo(new BigInteger(outTradeNo));
                order.setStatus(CONSTANT.ORDER_STATUS.PAID);
                order.setEndTime(LocalDateTime.now());
                int rows = orderMapper.update(order, updateWrapper);
                if (rows == 1) {
                    log.info("订单{}支付已完成,时间:{}", outTradeNo, LocalDateTime.now());
                    return CommonResponse.createForSuccess("异步通知:支付成功");
                }
            }
        }
        log.info("订单{}支付失败,时间:{}", outTradeNo, LocalDateTime.now());
        return CommonResponse.createForError("异步通知:支付失败");
    }
}

package org.csu.api.service.Impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.csu.api.domain.OrderItem;
import org.csu.api.persistence.OrderItemMapper;
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
import org.csu.api.service.MyAlipayService;
import org.csu.api.service.OrderService;
import org.csu.api.vo.QRCodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class MyAlipayServiceImpl implements MyAlipayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

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
    public CommonResponse<QRCodeVO> getQRCode(Integer userId, BigInteger orderNo) {

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("order_no", orderNo);
        Order order = orderMapper.selectOne(queryWrapper);

        if(order == null){
            return CommonResponse.createForError("订单不存在");
        }

        QueryWrapper<OrderItem> itemQueryWrapper = new QueryWrapper<>();
        itemQueryWrapper.eq("user_id",userId);
        itemQueryWrapper.eq("order_no", orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectList(itemQueryWrapper);
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = orderNo.toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = "Mall-api商城，订单号:" + orderNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPaymentPrice().toString();

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "用户自己的支付宝付款码"; // 条码示例，286648048691290423

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = "购买商品共" + order.getPaymentPrice() + "元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "120m";

        // todo: orderItemList-> goodsDetailList
        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
//        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx面包", 1000, 1);
//        // 创建好一个商品后添加至商品明细列表
//        goodsDetailList.add(goods1);
//
//        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
//        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);

        for(OrderItem orderItem : orderItemList) {
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    orderItem.getCurrentPrice().multiply(new BigDecimal(100)).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        String appAuthToken = "应用授权令牌";//根据真实值填写

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl("http://depzhm.natappfree.cc/alipay/notify_url")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        Configs.init("zfbinfo.properties");
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                String qrCodeBase64 = getQRImageByBase64(response.getQrCode());

                QRCodeVO qrCodeVO = new QRCodeVO();
                qrCodeVO.setOrderNo(orderNo);
                qrCodeVO.setQrCodeBase64(qrCodeBase64);

                return CommonResponse.createForSuccess(qrCodeVO);
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return CommonResponse.createForError("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return CommonResponse.createForError("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return CommonResponse.createForError("不支持的交易状态，交易返回异常!!!");
        }
    }

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

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    private String getQRImageByBase64(String qrImageCode) {
        String base64Image = "";

        try{
            Map<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrImageCode, BarcodeFormat.QR_CODE, 256, 256, hints);

            BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < 256; x++) {
                for (int y = 0; y < 256; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image,"png",os);

            base64Image = new String(new Base64().encode(os.toByteArray()));
            return base64Image;
        }catch(Exception  e){
            log.error("创建二维码图片错误",e);
            return null;
        }
    }
}

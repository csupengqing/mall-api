package org.csu.api.controller.front;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.service.AlipayService;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @RequestMapping(value = "/pay")
    public String pay(HttpSession session, HttpServletRequest request, BigInteger orderNo) throws AlipayApiException {
        needLogin(session);
        return alipayService.pay(orderNo);
    }

    // 同步通知，说明接口是否调用成功
    @GetMapping("/return_url")
    public CommonResponse<Object> returnUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        return alipayService.returnUrl(request);
    }

    // 异步通知，直接与服务器进行交互
    @PostMapping("/notify_url")
    public CommonResponse<String> notifyUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        return alipayService.notifyUrl(request);
    }

    private CommonResponse<Object> needLogin(HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return null;
    }

}

package org.csu.api.controller.front;

import com.alipay.api.AlipayApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.service.MyAlipayService;
import org.csu.api.vo.QRCodeVO;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private MyAlipayService myAlipayService;

    @GetMapping("get_qrcode")
    public CommonResponse<QRCodeVO> getQRCode(@RequestParam @NotNull(message = "订单号不能为空") BigInteger orderNo,
                                              HttpSession session) {
        UserVO loginUser = (UserVO) session.getAttribute(CONSTANT.LOGIN_USER);
        if (loginUser == null) {
            return CommonResponse.createForError(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDescription());
        }
        return myAlipayService.getQRCode(loginUser.getId(), orderNo);
    }

//    @RequestMapping(value = "/pay")
//    public String pay(HttpSession session, HttpServletRequest request, BigInteger orderNo) throws AlipayApiException {
//        needLogin(session);
//        return myAlipayService.pay(orderNo);
//    }
//
//    // 同步通知，说明接口是否调用成功
//    @GetMapping("/return_url")
//    public CommonResponse<Object> returnUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
//        return myAlipayService.returnUrl(request);
//    }

    // 异步通知，直接与服务器进行交互
    @PostMapping("/notify_url")
    public CommonResponse<String> notifyUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        return myAlipayService.notifyUrl(request);
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

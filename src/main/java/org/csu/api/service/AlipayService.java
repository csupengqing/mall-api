package org.csu.api.service;

import com.alipay.api.AlipayApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.csu.api.common.CommonResponse;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public interface AlipayService {
    String pay(BigInteger orderNo) throws AlipayApiException;
    CommonResponse<Object> returnUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException;
    CommonResponse<String> notifyUrl(HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException;
}

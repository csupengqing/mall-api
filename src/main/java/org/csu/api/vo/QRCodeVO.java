package org.csu.api.vo;

import lombok.Data;

import java.math.BigInteger;

@Data
public class QRCodeVO {
    private BigInteger orderNo;
    private String QrCodeBase64;
}

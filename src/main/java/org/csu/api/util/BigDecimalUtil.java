package org.csu.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {

    public static BigDecimal add(double a , double b){
        return new BigDecimal(Double.toString(a)).add(new BigDecimal(Double.toString(b)));
    }

    public static BigDecimal subtract(double a , double b){
        return new BigDecimal(Double.toString(a)).subtract(new BigDecimal(Double.toString(b)));
    }

    public static BigDecimal multiply(double a , double b){
        return new BigDecimal(Double.toString(a)).multiply(new BigDecimal(Double.toString(b)));
    }

    public static BigDecimal divide(double a , double b){
        BigDecimal a1 = new BigDecimal(Double.toString(a));
        BigDecimal b1 = new BigDecimal(Double.toString(b));
        return a1.divide(b1,2, RoundingMode.HALF_UP);
    }
}

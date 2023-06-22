package org.csu.api.common;

import lombok.Getter;

public class CONSTANT {

    public static final String LOGIN_USER = "loginUser";
    public static final String LOGIN_ADMIN = "loginAdmin";
    public static final int CATEGORY_ROOT = 0;

    public interface ROLE{
        int CUSTOMER = 1;
        int ADMIN = 0;
    }

    public interface USER_FIELD{
        String USERNAME = "username";
        String EMAIL = "email";
        String PHONE = "phone";
    }

    @Getter
    public enum ProductStatus{

        ON_SALE(1, "on_sale"),
        TAKE_DOWN(2, "take_down"),
        DELETE(3, "delete");

        private final int code;
        private final String description;

        ProductStatus(int code, String description){
            this.code = code;
            this.description = description;
        }
    }

    public static final String PRODUCT_ORDER_BY_PRICE_ASC = "price_asc";
    public static final String PRODUCT_ORDER_BY_PRICE_DESC = "price_desc";

    public static final String Order_ORDER_BY_TIME_ASC = "asc";
    public static final String Order_ORDER_BY_TIME_DESC = "desc";

    public interface CART_ITEM_STATUS{
        int CHECKED = 1;
        int UNCHECKED = 0;
    }

    public interface PAYMENT_TYPE {
        int ZHI_FU_BAO = 1;
        int WEI_XIN_ZHI_FU = 2;
        int OTHERS = 3;
    }

    public interface ORDER_STATUS {
        int CANCLED = 1;
        int UN_PAID = 2;
        int PAID = 3;
        int DELIVERED = 4;
        int TRADE_SUCCESS = 5;
        int CLOSED = 6;
    }

}

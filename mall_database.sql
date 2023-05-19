SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS mystore_address;
CREATE TABLE mystore_address (
id int NOT NULL AUTO_INCREMENT COMMENT '地址ID',
user_id int DEFAULT NULL COMMENT '用户ID',
address_name varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '收货人姓名',
address_phone varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '固定电话',
address_mobile varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '手机号码',
address_province varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '省份',
address_city varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '城市',
address_district varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '区县',
address_detail varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '详细地址',
address_zip varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '邮编',
create_time datetime DEFAULT NULL COMMENT '创建时间',
update_time datetime DEFAULT NULL COMMENT '最后修改时间',
PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_cart;
CREATE TABLE mystore_cart (
id int NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
user_id int NOT NULL COMMENT '用户ID，用户表主键',
product_id int NOT NULL COMMENT '商品ID，商品表主键',
quantity int NOT NULL COMMENT '数量',
checked tinyint(1) NOT NULL COMMENT '是否选择：1-选中，2-未选中',
create_time datetime NOT NULL COMMENT '创建时间',
update_time datetime NOT NULL COMMENT '最后一次修改时间',
PRIMARY KEY (id),
KEY user_id_index (user_id) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_category;
CREATE TABLE mystore_category (
id int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
parent_id int NOT NULL COMMENT '递归设计，父类别ID，id=0表示根节点',
name varchar(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '分类名称',
status tinyint(1) NOT NULL DEFAULT '1' COMMENT '分类状态：1-使用，2-废弃',
sort_order int DEFAULT NULL COMMENT '排序编号：同级别分类展示时的顺序',
create_time datetime NOT NULL COMMENT '创建时间',
update_time datetime NOT NULL COMMENT '最后一次更新时间',
PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=100031 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_order;
CREATE TABLE mystore_order (
id int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
order_no bigint NOT NULL COMMENT '订单号',
user_id int NOT NULL COMMENT '用户ID',
address_id int DEFAULT NULL COMMENT '订单地址ID',
payment_price decimal(20,2) DEFAULT NULL COMMENT '实际支付价格',
payment_type int DEFAULT NULL COMMENT '支付类型：1-支付宝,2-微信支付,3-其他扩展',
postage int DEFAULT NULL COMMENT '邮费',
status int DEFAULT NULL COMMENT '订单状态：1-已取消，2-未付款，3-已付款，4-已发货，5-交易成功，6-订单关闭',
payment_time datetime DEFAULT NULL COMMENT '支付时间',
send_time datetime DEFAULT NULL COMMENT '发货时间',
end_time datetime DEFAULT NULL COMMENT '交易完成时间',
close_time datetime DEFAULT NULL COMMENT '交易关闭时间',
create_time datetime DEFAULT NULL COMMENT '创建时间',
update_time datetime DEFAULT NULL COMMENT '最后修改时间',
PRIMARY KEY (id),
UNIQUE KEY order_no_index (order_no) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_order_item;
CREATE TABLE mystore_order_item (
id int NOT NULL AUTO_INCREMENT COMMENT '订单商品表ID',
user_id int DEFAULT NULL COMMENT '用户ID',
order_no bigint DEFAULT NULL COMMENT '订单号',
product_id int DEFAULT NULL COMMENT '商品ID',
product_name varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '商品名称',
product_image varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片URL',
current_price decimal(20,2) DEFAULT NULL COMMENT '订单中的商品单价',
quantity int DEFAULT NULL COMMENT '商品数量',
total_price decimal(20,2) DEFAULT NULL COMMENT '订单商品总价',
create_time datetime DEFAULT NULL COMMENT '创建时间',
update_time datetime DEFAULT NULL COMMENT '最后修改时间',
PRIMARY KEY (id),
KEY order_no_index (order_no) USING BTREE,
KEY order_no_user_id_index (user_id,order_no) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_pay_info;
CREATE TABLE mystore_pay_info (
id int NOT NULL AUTO_INCREMENT COMMENT '支付表ID',
user_id int DEFAULT NULL COMMENT '用户ID',
order_no bigint DEFAULT NULL COMMENT '订单号',
payment_type int DEFAULT NULL COMMENT '线上支付平台：1-支付宝，2-微信支付',
trade_no varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '线上平台返回支付流水号',
trade_status varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '线上平台返回支付状态或信息',
create_time datetime DEFAULT NULL COMMENT '创建时间',
update_time datetime DEFAULT NULL COMMENT '最后修改时间',
PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_product;
CREATE TABLE mystore_product (
id int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
category_id int NOT NULL COMMENT '分类ID，category表主键',
name varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '商品名称',
subtitle varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '商品副标题',
main_image varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '商品描述主图，url相对地址',
sub_images text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '商品描述图片，json字符串',
detail text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '商品详情',
price decimal(20,2) NOT NULL COMMENT '商品价格，2位小数',
stock int NOT NULL COMMENT '库存数量',
status int NOT NULL DEFAULT '1' COMMENT '商品状态：1-在售，2-下架，3-删除',
create_time datetime NOT NULL COMMENT '创建时间',
update_time datetime NOT NULL COMMENT '最后一次修改时间',
PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;


DROP TABLE IF EXISTS mystore_user;
CREATE TABLE mystore_user (
id int NOT NULL AUTO_INCREMENT COMMENT '用户信息表ID',
username varchar(30) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '用户名',
password varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '加密密码(MD5)',
email varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '用户Email',
phone varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '用户电话',
question varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '找回密码问题',
answer varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '找回密码答案',
role int NOT NULL COMMENT '用户角色0-管理员，1-普通用户',
create_time datetime NOT NULL COMMENT '用户注册时间',
update_time datetime NOT NULL COMMENT '最后更新时间',
PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1205530634 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

SET FOREIGN_KEY_CHECKS = 1;
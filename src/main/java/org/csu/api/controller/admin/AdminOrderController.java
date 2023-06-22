package org.csu.api.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.csu.api.common.CONSTANT;
import org.csu.api.common.CommonResponse;
import org.csu.api.common.ResponseCode;
import org.csu.api.service.OrderService;
import org.csu.api.vo.OrderVO;
import org.csu.api.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/admin/order")
@Validated
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public CommonResponse<Page<OrderVO>> list(@RequestParam(required = false) Integer orderStatus,
                                              @RequestParam(defaultValue = "desc") String orderBy,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "5") int pageSize,
                                              HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return orderService.list(orderStatus,orderBy,pageNum,pageSize);
    }
    //发货
    @PostMapping("/send")
    public CommonResponse<String> send(@RequestParam @NotNull(message = "订单ID不能为空") Integer orderId, HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return orderService.send(orderId);
    }
    //交易关闭
    @PostMapping("/close")
    public CommonResponse<String> close(@RequestParam @NotNull(message = "订单ID不能为空") Integer orderId, HttpSession session){
        UserVO userVO=(UserVO)session.getAttribute(CONSTANT.LOGIN_ADMIN);
        if(userVO == null){
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"管理员未登录");
        }
        else
            return orderService.close(orderId);
    }

}

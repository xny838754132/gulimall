package com.nai.gulimall.order.web;

import com.nai.gulimall.order.service.OrderService;
import com.nai.gulimall.order.vo.OrderConfirmVo;
import com.nai.gulimall.order.vo.OrderSubmitVo;
import com.nai.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author TheNai
 * @date 2021-03-22 23:17
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;


    @GetMapping("/toTrade")
    private String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * 下单功能
     *
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
        if (responseVo.getCode() == 0) {
            //下单成功,来到支付选择页面
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            //下单失败,回到订单确认页,重新确认订单信息
            String msg = "下单失败";
            switch (responseVo.getCode()) {
                default:
                    msg = msg;
                    break;
                case 1:
                    msg += "订单信息过去,请刷新再次提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化,请确认后再次提交";
                    break;
                case 3:
                    msg += "库存锁定失败,商品库存不足";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}

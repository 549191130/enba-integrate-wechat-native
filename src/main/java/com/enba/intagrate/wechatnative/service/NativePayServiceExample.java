package com.enba.intagrate.wechatnative.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.enba.intagrate.wechatnative.properties.EnbaMpProperties;
import com.enba.intagrate.wechatnative.properties.EnbaNativeProperties;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByIdRequest;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** NativePayService使用示例 */
@Service
@Slf4j
public class NativePayServiceExample {

  @Autowired private EnbaMpProperties enbaMpProperties;
  @Autowired private EnbaNativeProperties nativeProperties;
  @Autowired private NativePayService nativePayService;
  @Autowired public RefundService refundService;

  /** 关闭订单 */
  public void closeOrder(String outTradeNo) {

    CloseOrderRequest request = new CloseOrderRequest();
    request.setOutTradeNo(outTradeNo);
    request.setMchid(nativeProperties.getMerchantId());

    // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义
    // 调用接口
    nativePayService.closeOrder(request);
  }

  /** Native支付预下单 */
  public PrepayResponse prepay() {
    PrepayRequest request = new PrepayRequest();
    // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义

    // 【公众号ID】 公众号ID
    request.setAppid(enbaMpProperties.getAppId());
    // 【商户号】 商户号
    request.setMchid(nativeProperties.getMerchantId());
    // 【商品描述】 商品描述
    request.setDescription("商品描述");
    // 【商户订单号】 商户系统内部订单号，只能是数字、大小写字母_-*且在同一个商户号下唯一
    request.setOutTradeNo(IdUtil.fastSimpleUUID());
    // 【通知地址】 异步接收微信支付结果通知的回调地址，通知URL必须为外网可访问的URL，不能携带参数。 公网域名必须为HTTPS
    request.setNotifyUrl(nativeProperties.getNotifyUrl());
    // 订单金额信息
    Amount amount = new Amount();
    // 订单总金额，单位为分
    amount.setTotal(1);
    request.setAmount(amount);

    log.info("NativePayServiceExample.prepay###request:{}", JSON.toJSONString(request));

    // 调用接口
    PrepayResponse prepay = nativePayService.prepay(request);

    log.info("NativePayServiceExample.prepay###prepay:{}", JSON.toJSONString(prepay));

    return prepay;
  }

  /** 微信支付订单号查询订单 */
  public Transaction queryOrderById() {

    QueryOrderByIdRequest request = new QueryOrderByIdRequest();
    // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义
    // 调用接口
    return nativePayService.queryOrderById(request);
  }

  /** 商户订单号查询订单 */
  public com.wechat.pay.java.service.payments.model.Transaction queryOrderByOutTradeNo(
      String outTradeNo) {

    QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
    // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义

    request.setOutTradeNo(outTradeNo);
    request.setMchid(nativeProperties.getMerchantId());

    // 调用接口
    return nativePayService.queryOrderByOutTradeNo(request);
  }

  /** 退款申请 */
  public Refund refund(String outTradeNo) {
    CreateRequest request = new CreateRequest();
    // 商户订单号
    request.setOutTradeNo(outTradeNo);
    // 商户退款单号
    request.setOutRefundNo(IdUtil.fastSimpleUUID());
    request.setReason("测试");
    // 【退款结果回调url】 异步接收微信支付退款结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
    // 如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效，优先回调当前传的这个地址。
    //request.setNotifyUrl("");

    AmountReq amountReq = new AmountReq();
    amountReq.setRefund(1L);
    amountReq.setTotal(1L);
    amountReq.setCurrency("CNY");

    request.setAmount(amountReq);

    // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义
    // 调用接口
    return refundService.create(request);
  }

  /** 查询单笔退款（通过商户退款单号） */
  public Refund queryByOutRefundNo(String outRefundNo) {

    QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
    request.setOutRefundNo(outRefundNo);

    // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义
    // 调用接口
    return refundService.queryByOutRefundNo(request);
  }
}

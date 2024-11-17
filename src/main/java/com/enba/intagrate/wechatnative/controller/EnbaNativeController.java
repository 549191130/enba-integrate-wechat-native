package com.enba.intagrate.wechatnative.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson.JSON;
import com.enba.intagrate.wechatnative.properties.EnbaNativeProperties;
import com.enba.intagrate.wechatnative.service.NativePayServiceExample;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.partnerpayments.nativepay.model.Transaction;
import com.wechat.pay.java.service.partnerpayments.nativepay.model.Transaction.TradeStateEnum;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.refund.model.Refund;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单演示支付
 *
 * @author: enba
 * @description: 恩爸整合JSAPI
 */
@RequestMapping("/enba-native")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EnbaNativeController {

  private final EnbaNativeProperties nativeProperties;
  private final NativePayServiceExample nativePayServiceExample;

  // native下单
  @GetMapping("/pay-native")
  public PrepayResponse payNative() {

    return nativePayServiceExample.prepay();
  }

  /**
   * 返回二维码
   *
   * @param httpResponse r
   * @throws IOException io
   */
  @GetMapping("/pay-native/qr-code")
  public void payNativeQrCode(HttpServletResponse httpResponse) throws IOException {

    PrepayResponse prepay = nativePayServiceExample.prepay();

    QrCodeUtil.generate(prepay.getCodeUrl(), 256, 256, "", httpResponse.getOutputStream());
  }

  /*
   * 注意

  对后台通知交互时，如果微信收到应答不是成功或超时，微信认为通知失败，微信会通过一定的策略定期重新发起通知，尽可能提高通知的成功率，但微信不保证通知最终能成功

  同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。 推荐的做法是，当商户系统收到通知进行处理时，先检查对应业务数据的状态，并判断该通知是否已经处理。如果未处理，则再进行处理；如果已处理，则直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
  如果在所有通知频率后没有收到微信侧回调。商户应调用查询订单接口确认订单状态。

  特别提醒： 商户系统对于开启结果通知的内容一定要做签名验证，并校验通知的信息是否与商户侧的信息一致，防止数据泄露导致出现“假通知”，造成资金损失。*/
  @PostMapping("/pay-callback/abc")
  public ResponseEntity<CallbackResult> callback(HttpServletRequest request) throws IOException {

    InputStream inputStream = request.getInputStream();
    // BufferedReader是包装设计模式，BufferedReader带缓冲而且可以一行一行的读，性能更高
    // stream和reader之间的转换需要一个转换流，InputStreamReader
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    StringBuffer sb = new StringBuffer();
    String line;
    while ((line = in.readLine()) != null) {
      sb.append(line);
    }
    in.close();
    inputStream.close();

    // 构造 RequestParam
    RequestParam requestParam =
        new RequestParam.Builder()
            .serialNumber(request.getHeader("Wechatpay-Serial"))
            .nonce(request.getHeader("Wechatpay-Nonce"))
            .signature(request.getHeader("Wechatpay-Signature"))
            .timestamp(request.getHeader("Wechatpay-Timestamp"))
            .body(sb.toString())
            .build();

    // 如果已经初始化了 RSAAutoCertificateConfig，可直接使用
    // 没有的话，则构造一个
    NotificationConfig config =
        new RSAAutoCertificateConfig.Builder()
            .merchantId(nativeProperties.getMerchantId())
            .privateKeyFromPath(nativeProperties.getPrivateKeyPath())
            .merchantSerialNumber(nativeProperties.getMerchantSerialNumber())
            .apiV3Key(nativeProperties.getApiV3Key())
            .build();

    // 初始化 NotificationParser
    NotificationParser parser = new NotificationParser(config);
    try {
      // 以支付通知回调为例，验签、解密并转换成 Transaction
      Transaction transaction = parser.parse(requestParam, Transaction.class);

      log.info("transaction: {}", JSON.toJSONString(transaction));

      // TODO 判断支付是否成功，处理自己的业务逻辑
      if (TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
        log.info("pay success");

        return ResponseEntity.status(HttpStatus.OK).body(CallbackResult.ok());
      } else {
        log.error("pay failed");
      }
    } catch (ValidationException e) {
      // 签名验证失败，返回 401 UNAUTHORIZED 状态码
      log.error("sign verification failed", e);

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CallbackResult.err());
    }

    // 处理成功，返回 200 OK 状态码
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CallbackResult.err());
  }

  @Data
  public static class CallbackResult {

    private String code;

    private String message;

    public static CallbackResult ok() {
      CallbackResult callbackResult = new CallbackResult();
      callbackResult.setCode("SUCCESS");
      callbackResult.setMessage("成功");
      return callbackResult;
    }

    public static CallbackResult err() {
      CallbackResult callbackResult = new CallbackResult();
      callbackResult.setCode("FAIL");
      callbackResult.setMessage("失败");
      return callbackResult;
    }
  }

  // 根据商户号查询订单
  @GetMapping("/query-order/out-trade-no")
  public com.wechat.pay.java.service.payments.model.Transaction queryOrderById(
      @org.springframework.web.bind.annotation.RequestParam String outTradeNo) {

    return nativePayServiceExample.queryOrderByOutTradeNo(outTradeNo);
  }

  /*
    * 关闭订单，以下情况需要调用关单接口：

  商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
  系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
    * */
  @GetMapping("/close")
  public String close(String outTradeNo) {

    nativePayServiceExample.closeOrder(outTradeNo);

    return "退款成功";
  }

  /*
    * 当交易发生之后一段时间内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付款退还给买家，微信支付将在收到退款请求并且验证成功之后，按照退款规则将支付款按原路退到买家帐号上。

  注意：

  交易时间超过一年的订单无法提交退款（按支付成功时间+365天计算）
  微信支付退款支持单笔交易分多次退款，多次退款需要提交原支付订单的商户订单号和设置不同的退款单号。申请退款总金额不能超过订单金额。 一笔退款失败后重新提交，请不要更换退款单号，请使用原商户退款单号
  请求频率限制：150qps，即每秒钟正常的申请退款请求次数不超过150次
  每个支付订单的部分退款次数不能超过50次
  如果同一个用户有多笔退款，建议分不同批次进行退款，避免并发退款导致退款失败
  申请退款接口的返回仅代表业务的受理情况，具体退款是否成功，需要通过退款查询接口获取结果
  错误或无效请求频率限制：6qps，即每秒钟异常或错误的退款申请请求不超过6次
  一个月之前的订单申请退款频率限制为：5000/min
  同一笔订单多次退款的请求需相隔1分钟
    * */
  @GetMapping("/refund")
  public Refund refund(String outTradeNo) {

    return nativePayServiceExample.refund(outTradeNo);
  }

  /*
   * 提交退款申请后，通过调用该接口查询退款状态。退款有一定延时，建议查询退款状态在提交退款申请后1分钟发起，一般来说零钱支付的退款5分钟内到账，银行卡支付的退款1-3个工作日到账。
   * */
  @GetMapping("/query-refund")
  public Refund queryRefund(String outRefundNo) {

    return nativePayServiceExample.queryByOutRefundNo(outRefundNo);
  }
}

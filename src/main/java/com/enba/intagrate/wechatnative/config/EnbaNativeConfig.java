package com.enba.intagrate.wechatnative.config;

import com.enba.intagrate.wechatnative.properties.EnbaNativeProperties;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnbaNativeConfig {

  @Bean
  public NativePayService nativePayService(EnbaNativeProperties properties) {
    Config config =
        new RSAAutoCertificateConfig.Builder()
            .merchantId(properties.getMerchantId())
            // 使用 com.wechat.pay.java.core.util 中的函数从本地文件中加载商户私钥，商户私钥会用来生成请求的签名
            .privateKeyFromPath(properties.getPrivateKeyPath())
            .merchantSerialNumber(properties.getMerchantSerialNumber())
            .apiV3Key(properties.getApiV3Key())
            .build();

    // 初始化服务
    return new NativePayService.Builder().config(config).build();
  }

  @Bean
  public RefundService refundService(EnbaNativeProperties properties) {
    Config config =
        new RSAAutoCertificateConfig.Builder()
            .merchantId(properties.getMerchantId())
            // 使用 com.wechat.pay.java.core.util 中的函数从本地文件中加载商户私钥，商户私钥会用来生成请求的签名
            .privateKeyFromPath(properties.getPrivateKeyPath())
            .merchantSerialNumber(properties.getMerchantSerialNumber())
            .apiV3Key(properties.getApiV3Key())
            .build();

    // 初始化服务
    return new RefundService.Builder().config(config).build();
  }
}

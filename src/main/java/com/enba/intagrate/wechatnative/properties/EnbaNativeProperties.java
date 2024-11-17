package com.enba.intagrate.wechatnative.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "enba.jsapi")
@Data
public class EnbaNativeProperties {

  /** 商户号 */
  private String merchantId;

  /** 商户私钥路径 */
  private String privateKeyPath;

  /** 商户API证书序列号 */
  private String merchantSerialNumber;

  /** APIv3密钥 */
  private String apiV3Key;

  /** 支付回调地址 */
  private String notifyUrl;
}

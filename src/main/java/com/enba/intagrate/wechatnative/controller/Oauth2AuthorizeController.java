package com.enba.intagrate.wechatnative.controller;

import com.alibaba.fastjson.JSONObject;
import com.enba.intagrate.wechatnative.dto.Oauth2AccessTokenDTO;
import com.enba.intagrate.wechatnative.properties.EnbaMpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 * @author: enba
 * @description: 网页授权
 */
@RequestMapping("/enba-native")
@Controller
@Slf4j
public class Oauth2AuthorizeController {

  private final RestTemplate restTemplate;

  private final EnbaMpProperties enbaMpProperties;

  public Oauth2AuthorizeController(RestTemplate restTemplate, EnbaMpProperties enbaMpProperties) {
    this.restTemplate = restTemplate;
    this.enbaMpProperties = enbaMpProperties;
  }

  /**
   * 返回演示界面
   *
   * @param model Model
   * @return return
   */
  @RequestMapping("/index")
  public String show(Model model) {
    model.addAttribute("title", "网页授权演示");

    // 静默授权
    String jingMoUrl =
        "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
            + enbaMpProperties.getAppId()
            + "&redirect_uri="
            + enbaMpProperties.getRedirectUri()
            + "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
    model.addAttribute("jingMoUrl", jingMoUrl);

    // 非静默授权
    String noJingMoUrl =
        "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
            + enbaMpProperties.getAppId()
            + "&redirect_uri="
            + enbaMpProperties.getRedirectUri()
            + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
    model.addAttribute("noJingMoUrl", noJingMoUrl);

    return "index";
  }

  // 接收微信授权回调
  @ResponseBody
  @GetMapping("/login-callback")
  public String callback(String code, String state) {
    // 第一步：用户同意授权，获取code
    log.info("Received code: {}, state：{}", code, state);

    // 第二步：通过code换取网页授权access_token
    String url =
        "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    String formatUrl =
        String.format(url, enbaMpProperties.getAppId(), enbaMpProperties.getAppSecret(), code);

    String forObject = restTemplate.getForObject(formatUrl, String.class);
    log.info("接收到的响应：{}", forObject);

    Oauth2AccessTokenDTO oauth2AccessTokenDTO =
        JSONObject.parseObject(forObject, Oauth2AccessTokenDTO.class);

    log.info("网页授权oauth2AccessTokenDTO：{}", oauth2AccessTokenDTO);
    /*{
        "access_token": "84_PZYp7AJGguLp_ISYyqx2IGin-_ytDqljS_WEnWH1ITt-4bVnbbdFdg3eVNlun5MNqszo7WPNHHXSSGXODB7FB739Cm8RM9h3cx6O9DdDju4",
        "expires_in": 7200,
        "refresh_token": "84_XtY5znduvYwB8_6UferZBRjmEudeWDyrEIbTGh3Zmby-my6bF23knnvj7BH5S9C9GfMIjRrg_OqQ1to_-GA0bQhJkZptIneYiCwAe-lqlN8",
        "openid": "oEFbN6Kch-_VScWaQ7VIjPmKYQGU",
        "scope": "snsapi_userinfo"
        }
    */

    // 第三步：拉取用户信息(需scope为 snsapi_userinfo)
    String userInfoUrl =
        "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";
    String format =
        String.format(
            userInfoUrl, oauth2AccessTokenDTO.getAccess_token(), oauth2AccessTokenDTO.getOpenid());

    String forObject1 = restTemplate.getForObject(format, String.class);
    log.info("用户信息：{}", forObject1);
    /*
     * {"openid":"oEFbN6Kch-_VScWaQ7VIjPmKYQGU","nickname":"恩爸","sex":0,"language":"","city":"","province":"","country":"","headimgurl":"https:\/\/thirdwx.qlogo.cn\/mmopen\/vi_32\/0KtDGJwZEcic6GlSdic6UJN2NuXuVw6VXEIL8hiaCLysMFCiaPJgod8rYL9cd8iarbRWvojnC1iaic3ZpqGhRmfibnKJ4T6NReibT5Yv2m4FqZofxyo\/132","privilege":[]}
     * */

    // TODO 拿到微信用户的信息之后，做自己的业务逻辑

    return "演示网页授权";
  }
}

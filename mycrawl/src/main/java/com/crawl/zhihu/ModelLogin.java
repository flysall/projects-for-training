package com.crawl.zhihu;

import com.crawl.core.util.Config;
import com.crawl.core.util.HttpClientUtil;
import com.crawl.core.util.SimpleLogger;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class ModelLogin {
	private static Logger logger = SimpleLogger.getSimpleLogger(ModelLogin.class);
	// 邮箱登录地址
	final private static String EMAIL_LOGIN_URL = "https://www.zhihu.com/login/email";
	// 手机号登录地址
	final private static String PHONENUM_LOGIN_URL = "https://www.zhihu.com/login/phone_num";
	// 验证码地址
	final private static String YZM_URL = "https://www.zhihu.com/captcha.gif?type=login";

	/**
	 * @param emailOrPhoneNum
	 *            邮箱或手机号码
	 * @param pwd
	 *            密码
	 * @return
	 */
	public boolean login(String emailOrPhoneNum, String pwd) {
		String loginState = null;
		Map<String, String> postParams = new HashMap<>();
		String yzm = null;
		yzm = yzm(YZM_URL); // 肉眼识别验证码
		postParams.put("captcha", yzm);
		postParams.put("_xsrf", "");
		postParams.put("password", pwd);
		postParams.put("remember_me", "true");
		if (emailOrPhoneNum.contains("@")) {
			// 通过邮箱验证
			postParams.put("email", emailOrPhoneNum);
			try {
				loginState = HttpClientUtil.postRequest(EMAIL_LOGIN_URL, postParams); // 登录成功
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// 通过手机号登录
			postParams.put("phone_num", emailOrPhoneNum);
			try {
				loginState = HttpClientUtil.postRequest(PHONENUM_LOGIN_URL, postParams);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		JSONObject jo = (JSONObject) JSONValue.parse(loginState);
		if (jo.get("r").toString().equals("0")) {
			logger.info("登录知乎成功");
			// 序列化Cookies
			HttpClientUtil.serializeObject(HttpClientUtil.getCookieStore(), Config.cookiePath);
			return true;
		} else {
			logger.info("登录知乎失败");
			throw new RuntimeException(HttpClientUtil.decodeUnicode(loginState));
		}
	}

	/**
	 * 肉眼识别验证码
	 */
	public String yzm(String url) {
		String verificationCodePath = Config.verificationCodePath;
		String path = verificationCodePath.substring(0, verificationCodePath.lastIndexOf("/") + 1);
		String fileName = verificationCodePath.substring(verificationCodePath.lastIndexOf("/") + 1);
		HttpClientUtil.downloadFile(url, path, fileName, true);
		logger.info("请输入 " + verificationCodePath + "下的验证码：");
		Scanner sc = new Scanner(System.in);
		String yzm = sc.nextLine();
		return yzm;
	}

}

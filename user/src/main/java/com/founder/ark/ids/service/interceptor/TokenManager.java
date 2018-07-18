package com.founder.ark.ids.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.founder.ark.common.utils.HttpUtil;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.admin.user.dao.UserDao;
import com.founder.ark.ids.service.core.bean.ConstantsLibrary;
import com.founder.ark.ids.service.core.bean.keycloak.User;
import com.founder.ark.ids.service.core.util.HttpClientUtil;
import com.founder.ark.ids.service.core.util.NotCheckToken;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.NotAuthorizedException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by cheng.ly on 2018/4/8.
 */
@RestController
@RequestMapping(value = "/kc/token")
public class TokenManager {

    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String company;
    @Value("${ids.keycloak.serverUrl}")
    private String serverUrl;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JedisPool jedisPool;

    Logger logger = LoggerFactory.getLogger(TokenManager.class);

    /**
     * 通过IDS用户凭证获取Token
     */
    @RequestMapping(value = "/generateWithConfidential", method = RequestMethod.GET)
    @NotCheckToken
    public ResponseObject generateWithConfidential(@RequestHeader String UserName, @RequestHeader String Password) {
        try {
            logger.info("校验"+UserName+"凭证.");
            Keycloak.getInstance(serverUrl, company, UserName, Password, "admin-cli").tokenManager().getAccessToken();
        } catch (Exception e) {
            logger.info("凭证校验失败。");
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USERNAME_OR_PASSWORD_INCORRECT, ConstantsLibrary.Message.USERNAME_OR_PASSWORD_INCORRECT);
        }
        String idsToken = UUID.randomUUID().toString();
        User user = userDao.findByUsername(UserName);
        if (user == null) {
            logger.info("本地数据库不存在该用户。");
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USERNAME_OR_PASSWORD_INCORRECT, "用户不存在。");
        }
        redisAddProcess(user.getUsername(), user.getEmail(), idsToken);
        return ResponseObject.newSuccessResponseObject(idsToken, "Success.");
    }

    /**
     * Liferay用户登录时调用的获取Token
     */
    @RequestMapping(value = "/generateWithAccessToken", method = RequestMethod.GET)
    @NotCheckToken
    public String generateWithAccessToken(@RequestHeader String accessToken) {
        String idsToken = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "bearer " + accessToken);
        logger.info("验证AccessToken的有效性。");
        JSONObject jsonObject = HttpClientUtil.get(serverUrl+"/realms/"+company+"/protocol/openid-connect/userinfo", null, headers);
        if (jsonObject.get("error") == null) {
            String email = jsonObject.getString("email");
            String userName = jsonObject.getString("name");
            logger.info("验证AccessToken成功，用户名："+userName);
            try {
                redisAddProcess(userName, email, idsToken);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return idsToken;
        }
        return null;
    }


    /**
     * Liferay用户注销时过期token
     */
    @RequestMapping(value = "/expireToken", method = RequestMethod.DELETE)
    @NotCheckToken
    public ResponseObject expireToken(@RequestHeader String founderAuthToken) {
        try {
            redisExpireProcess(founderAuthToken);
        } catch (Exception e) {
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
        return ResponseObject.newSuccessResponseObject(null, "Success.");
    }

    private void redisAddProcess(String username, String email, String idsToken) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.hset(idsToken, "valid", "true");
            jedis.hset(idsToken, "email", email);
            jedis.hset(idsToken, "name", username);
            jedis.expire(idsToken, 1800);
        } catch (TimeoutException e) {
            logger.info("redis exception", e);
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    private void redisExpireProcess(String founderAuthToken) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis.hgetAll(founderAuthToken) != null) {
                jedis.expire(founderAuthToken, 0);
            }
        } catch (TimeoutException e) {
            logger.info("redis exception", e);
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }
}

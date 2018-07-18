package com.founder.ark.ids.service.core.interceptor;


import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.core.bean.ConstantsLibrary;
import com.founder.ark.ids.service.core.util.NotCheckToken;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

/**
 * Created by cheng.ly on 2018/4/9.
 */

public class ControllerInterceptor implements HandlerInterceptor {

    @Value("${redis.check}")
    String isCheckEnabled;

    @Autowired
    private  JedisPool jedisPool;

    private final static Logger logger = LoggerFactory.getLogger(ControllerInterceptor.class);


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        HandlerMethod method = (HandlerMethod) handler;
        NotCheckToken notCheckToken = method.getMethod().getAnnotation(NotCheckToken.class);
        if (notCheckToken == null) {
            if (!isTokenValid(httpServletRequest)) {
                logger.info("校验失败，令牌失效。");
                ResponseObject result = ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.TOKEN_INVALID, ConstantsLibrary.Message.TOKEN_INVALID);
                JSONObject jsonObject = new JSONObject(result);
                httpServletResponse.setCharacterEncoding("utf-8");
                PrintWriter out = httpServletResponse.getWriter();
                out.println(jsonObject);
                out.flush();
                out.close();
                return false;
            }
            logger.info("校验成功，方法："+method.getMethod().getName());
        }
        return true;
    }

    private boolean isTokenValid(HttpServletRequest request) {
        String idsToken = request.getHeader("ids-token")==null ?request.getHeader("authToken"):request.getHeader("ids-token");
        logger.info("idsToken:"+idsToken);
        if (idsToken == null) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String result = jedis.hget(idsToken,"valid");
            if (result == null || "false".equals(result)) {
                return false;
            }
            jedis.expire(idsToken,1800);
        } catch (TimeoutException e) {
            logger.info("redis exception",e);
            return false;
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}

package com.founder.ark.ids.interceptor;


import com.alibaba.fastjson.JSONObject;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.util.NotCheckToken;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ControllerInterceptor implements HandlerInterceptor {

    @Value("${redis.check}")
    boolean isCheckEnabled;

    @Autowired
    private JedisPool jedisPool;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("CheckToken开启状态： {}", isCheckEnabled);
        }
        if (!isCheckEnabled) return true;
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            NotCheckToken notCheckToken = method.getMethod().getAnnotation(NotCheckToken.class);
            if (notCheckToken == null) {
                if (!isTokenValid(httpServletRequest)) {
                    if (log.isDebugEnabled()) {
                        log.debug("校验失败，令牌失效。");
                    }
                    ResponseObject result = ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.TOKEN_INVALID, ConstantsLibrary.Message.TOKEN_INVALID);
                    httpServletResponse.setCharacterEncoding("utf-8");
                    PrintWriter out = httpServletResponse.getWriter();
                    out.println(JSONObject.toJSON(result));
                    out.flush();
                    out.close();
                    return false;
                }
                if (log.isDebugEnabled()) {
                    log.debug("校验成功，方法：" + method.getMethod().getName());
                }
            }
        }
        return true;
    }

    private boolean isTokenValid(HttpServletRequest request) {
        String idsToken = request.getHeader("ids-token") == null ? request.getHeader("authToken") : request.getHeader("ids-token");
        log.info("idsToken:" + idsToken);
        if (idsToken == null) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String result = jedis.hget(idsToken, "valid");
            if (result == null || "false".equals(result)) {
                return false;
            }
            jedis.expire(idsToken, 1800);
        } catch (TimeoutException e) {
            log.error("redis exception", e);
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

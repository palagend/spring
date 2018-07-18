package com.founder.ark.ids.service.core.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class HttpClientUtil {

    public static void sendMail(String mailServerUrl, String body, MediaType mediaType) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        HttpEntity request = new HttpEntity(body, headers);
        String result = restTemplate.postForObject(mailServerUrl, request, String.class);
        LoggerFactory.getLogger(HttpClientUtil.class).info(result);
    }

    public static void sendMail(String server, String to, String subject, String content) {
        String body = "to=" + to + "&from=no-replay@founder.com&subject=" + subject + "&body=" + content;
        sendMail(server, body, MediaType.APPLICATION_FORM_URLENCODED);
    }

    public static JSONObject get(String url, JSONObject params, HttpHeaders headers) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(expandURL(url, params), HttpMethod.GET, new HttpEntity<>(headers), JSONObject.class);
        return responseEntity.getBody();
    }

    public static String post(String url, JSONObject params, HttpHeaders headers, JSONObject body, MediaType mediaType) {
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(mediaType);
        HttpEntity<JSONObject> requestEntity = (mediaType == MediaType.APPLICATION_JSON
                || mediaType == MediaType.APPLICATION_JSON_UTF8) ? new HttpEntity<>(body, headers)
                : new HttpEntity<>(null, headers);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        String expandUrl = expandURL(url, params);
        ResponseEntity<String> responseEntity = restTemplate.exchange(expandUrl, HttpMethod.POST, requestEntity, String.class);
        HttpStatus result = responseEntity.getStatusCode();
        return result.toString();
    }

    public static <T> T post(String url, JSONObject params, HttpHeaders headers, JSONObject body, MediaType mediaType, Class<T> clz) {
        RestTemplate restTemplate = new RestTemplate();
        headers.setContentType(mediaType);
        HttpEntity requestEntity = new HttpEntity<>(body, headers);
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        String expandUrl = expandURL(url, params);
        T result = restTemplate.postForObject(expandUrl, requestEntity, clz);
        return result;
    }

    private static MultiValueMap<String, String> createMultiValueMap(JSONObject params) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (String key : params.keySet()) {
            if (params.get(key) instanceof List) {
                for (Iterator<String> it = ((List<String>) params.get(key)).iterator(); it.hasNext(); ) {
                    String value = it.next();
                    map.add(key, value);
                }
            } else {
                map.add(key, params.getString(key));
            }
        }
        return map;
    }

    private static String expandURL(String url, JSONObject params) {
        if (params == null) {
            return url;
        }
//        final Pattern QUERY_PARAM_PATTERN = Pattern.compile(".*[^?]");
//        Matcher mc = QUERY_PARAM_PATTERN.matcher(url);
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.getString(key)).append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static class DefaultResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            int code = response.getStatusCode().value();
            return !(code == HttpServletResponse.SC_OK || code == HttpServletResponse.SC_UNAUTHORIZED || code == HttpServletResponse.SC_CREATED);
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody()));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            try {
                throw new Exception(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
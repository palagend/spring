package com.founder.ark.ids.avatar.representations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求的返回结果
 *
 * @Autor huyh@founder.com
 * @Date 2018-08-08
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseObject<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    //状态码，0为正常，-1为未知错误，系统级异常；
    //大于0为具体错误，1000以内为公共定义的错误，1000以上为本接口定义的错误
    private int status;
    //描述
    private String message;
    //具体数据，对象/列表/其他
    private Object data;

    /**
     * 构造一个错误响应对象
     *
     * @param status       状态码
     * @param errorMessage 错误信息
     * @return ResponseObject instance
     */
    public static <T> ResponseObject<T> newErrorResponseObject(int status, String errorMessage) {
        ResponseObject<T> res = new ResponseObject<>();
        res.setStatus(status);
        res.setMessage(errorMessage);
        return res;
    }

    /**
     * 构造一个成功响应对象
     *
     * @param resultObject 具体返回内容
     * @param message      描述信息，如果传多个会用“\n”拼接
     * @return ResponseObject instance
     */
    public static <T> ResponseObject<T> newSuccessResponseObject(Object resultObject, String... message) {
        ResponseObject<T> res = new ResponseObject<>();
        res.setStatus(0);
        if (message != null && message.length > 0) {
            StringBuilder msgInfo = new StringBuilder();
            for (String msg : message) {
                msgInfo.append("\n").append(msg);
            }
            res.setMessage(msgInfo.toString());
        }
        res.setData(resultObject);
        return res;
    }
}
package com.founder.ark.ids.avatar.representations;

import lombok.Data;

/**
 * 封装邮件的类
 *
 * @author huyh@founder.com
 * @date 2018-08-14
 */
@Data
public class MailBean {
    String from;
    String to;
    String cc;
    String subject;
    String body;

    public MailBean(String to, String subject, String body, String cc, String from) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.body = body;
    }
}

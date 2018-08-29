package com.founder.ark.ids.avatar.service;


import com.founder.ark.ids.avatar.representations.MailBean;

/**
 * 发送邮件的服务类
 *
 * @author 胡月恒
 * @mail huyh@founder.com
 * @date 2018-08-14
 */
public interface Mailer {
    void send(MailBean bean);
}

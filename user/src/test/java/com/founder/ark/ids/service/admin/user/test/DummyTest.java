package com.founder.ark.ids.service.admin.user.test;

import com.founder.ark.UserApplication;
import com.founder.ark.ids.service.admin.user.tool.MailBean;
import com.founder.ark.ids.service.admin.user.tool.Mailer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UserApplication.class)
public class DummyTest {

    private static final String IDS_USERNAME_PLACEHOLDER = "ids.user";
    private static final String IDS_PASSWORD_PALCEHOLDER = "dddddddd";
    @LocalServerPort
    private int port;
    @Autowired
    Mailer mailer;
    @Value("${ids.home.url: http://ids.fzyun.io}")
    private String idsHomeUrl;

    @Test
    public void test() {
        MailBean bean = new MailBean();
        bean.setTo("huyh@founder.com");
        bean.setFrom("no-reply@founder.com");
        bean.setSubject("IDS账户激活邮件");
        bean.setBody("<p>尊敬的用户，您好：</p>\n" +
                "<p style='padding-left: 2em;'>欢迎您使用IDS系统，您的IDS账户已生成。</p>\n" +
                "<hr>\n" +
                "<p style=\"text-align: center;\">您的登录名：" + IDS_USERNAME_PLACEHOLDER + "</p>\n" +
                "<p style=\"text-align: center;\">您的临时密码：" + IDS_PASSWORD_PALCEHOLDER + "</p>\n" +
                "<hr>\n" +
                "<p style=\"text-align: center;\">访问地址：<a>" + idsHomeUrl + "</a></p>\n" +
                "<p style=\"text-align: center;\">为了您的账户安全，请您及时登录IDS系统并修改密码！</p>\n" +
                "<hr>\n" +
                "<p>如果您访问账户遇问题，联您系管理员。</p>\n" +
                "<p>这是有IDS系统自动生成的邮件，请勿回复。</p>");
        mailer.sendmail(bean);
    }

    public static void main(String[] args) {
        boolean b = ".liu.d-z@founder.com".matches("^(?![_.-])[A-Za-z0-9_.-]+(?<![_.-])@([_A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,3}$");
        System.out.println(b);
    }

}

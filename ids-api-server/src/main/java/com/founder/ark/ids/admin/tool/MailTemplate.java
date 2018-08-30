package com.founder.ark.ids.admin.tool;

public class MailTemplate {
    public static final int INITIAL_PASSWORD = 1;
    public static final int RESET_PASSWORD = 2;
    private String email;
    private String from;
    private String subject;
    private String body;

    public MailTemplate(String username, String email, String pwd, String idsHomeUrl, String kbHomeUrl, int type) {
        this.email = email;
        this.from = "no-reply@founder.com";
        if (type == INITIAL_PASSWORD) {
            this.subject = "欢迎使用IDS系统";
            this.body = "<p>尊敬的用户，您好：</p>" +
                    "<p style='padding-left:2em'>欢迎您使用IDS系统，您的IDS账户已生成。</p>" +
                    "<hr>" +
                    "<p style='text-align: center;'>登录名：" + username + "</p>" +
                    "<p style='text-align: center;'>临时密码：" + pwd + "</p>" +
                    "<hr>" +
                    "<p style='text-align: center;'>为了账户安全，请及时<strong>登录IDS门户系统并修改密码</strong>！</p>" +
                    "<p style='text-align: center;'>IDS门户系统访问地址：<a href='" + idsHomeUrl +
                    "'>" + idsHomeUrl + "</a></p>" +
                    "<p style='text-align: center;'>您可以使用IDS账号登录使用方正电子知识库、IDS门户系统等。</p>" +
                    "<p style='text-align: center;'>知识库访问地址：<a href='" + kbHomeUrl +
                    "'>" + kbHomeUrl + "</a></p>" +
                    "<p style='text-align: center;'><strong>如果您在使用IDS账户过程中需要修改密码，请登录IDS门户系统完成密码更新</strong>。</p>" +
                    "<p>如果您在访问帐户时遇到问题，请联系管理员。</p>" +
                    "<p>这是由IDS系统自动生成的邮件，请勿回复。</p>";
        } else if (type == RESET_PASSWORD) {
            this.subject = "账户密码重置";
            this.body = "<p>尊敬的用户，您好：</p>" +
                    "<p style='padding-left: 2em;'>您的IDS账户密码已重置，为了账户安全，请点击以下地址修改密码。</p>" +
                    "<p style='padding-left: 2em;'>访问地址：<a href='" + idsHomeUrl + "'>" + idsHomeUrl + "</a> 临时密码为：" + pwd + "</p>" +
                    "<p style='padding-left: 2em;'>如果您在访问帐户时遇到问题，请联系管理员。</p>" +
                    "<p style='padding-left: 2em;'>这是由IDS系统自动生成的邮件，请勿回复。</p>";
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

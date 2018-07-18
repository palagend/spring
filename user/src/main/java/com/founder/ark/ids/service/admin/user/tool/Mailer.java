package com.founder.ark.ids.service.admin.user.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Mailer {
    Gson gson = new GsonBuilder().create();
    @Autowired
    private RabbitTemplate rabbitTemplate;//需要在配置文件中增加RabbitMQ的信息

    public void sendmail(MailBean mailBean) {
        String json = gson.toJson(mailBean);
        rabbitTemplate.convertAndSend("mailQueue", json);//将json格式的字符发送到名为“myQueue”的队列中
    }
}

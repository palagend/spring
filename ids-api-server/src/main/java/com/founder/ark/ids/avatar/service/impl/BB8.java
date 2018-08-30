package com.founder.ark.ids.avatar.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founder.ark.ids.Athena;
import com.founder.ark.ids.avatar.representations.GroupWrapper;
import com.founder.ark.ids.avatar.representations.MailBean;
import com.founder.ark.ids.avatar.representations.UserWrapper;
import com.founder.ark.ids.avatar.service.KeycloakFacade;
import com.founder.ark.ids.avatar.service.Mailer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * <p>\uD83C\uDF0D《星球大战》中的机器人 BB-8 </p>
 * <p>全能手</p>
 *
 * @author 胡月恒
 * @mail huyh@founder.com
 * @date 2018-08-14
 */
@Slf4j
@Component
@RefreshScope
public class BB8 implements KeycloakFacade, Mailer {
    @Autowired
    private RabbitTemplate rabbitTemplate;//需要在配置文件中增加RabbitMQ的信息
    @Value("${avatar.keycloak.realm}")
    private String realm;
    @Autowired
    private Keycloak keycloak;
    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Override
    public Response createOrUpdate(UserWrapper user) {
        String password = RandomStringUtils.randomAlphanumeric(8);
        user.setPassword(password);
        Response resp = keycloak.realm(realm).users().create(user.unwrap());
        if (log.isDebugEnabled()) {
            log.debug("[CREATING USER]: response message is ({})", resp.getStatusInfo());
        }
        if (resp.getStatus() == HttpServletResponse.SC_CREATED) {
            Object[] args = {user.getUsername(), password};
            MailBean bean = new MailBean(user.getEmail()
                    , messageSource.getMessage("avatar.mail.createUser.subject"
                    , null, LocaleContextHolder.getLocale())
                    , messageSource.getMessage("avatar.mail.createUser.body", args, LocaleContextHolder.getLocale())
                    , null, messageSource.getMessage("avatar.mail.from", null, LocaleContextHolder.getLocale()));
            send(bean);
        }
        return resp;
    }

    @Override
    public Response createOrUpdate(GroupWrapper group) {
        log.trace("The group id is ({})", group.unwrap().getId());
        if (StringUtils.isEmpty(group.getId()))
            return keycloak.realm(realm).groups().add(group.unwrap());
        else {
            keycloak.realm(realm).groups().group(group.getId()).update(group.unwrap());
            return Response.ok().build();
        }
    }

    @Override
    public Response delete(String id) {
        Response resp = keycloak.realm(realm).users().delete(id);
        if (log.isDebugEnabled()) {
            log.debug("[DELETING USER]: id is ({}), response message is ({})", id, resp.getStatusInfo());
        }
        return resp;
    }

    @Override
    public UserWrapper findByUsername(String username) {
        List<UserRepresentation> lst = keycloak.realm(realm).users().search(username);
        if (lst != null && lst.size() > 0) {
            for (UserRepresentation u : lst) {
                if (u.getUsername().equals(username)) {
                    return UserWrapper.wrap(u);
                }
            }
        }
        return null;
    }

    @Override
    public PagedListHolder<UserRepresentation> list(String queryString) {
        if (log.isDebugEnabled()) {
            log.debug("当前keycloak示例操作的域（realm）是：{}", realm);
        }
        List<UserRepresentation> lst = keycloak.realm(realm).users().search(queryString, 0, Athena.MAX_RESULTS);
        PagedListHolder page = new PagedListHolder();
        page.setSource(lst);
        return page;
    }

    @Override
    public UserWrapper detail(String id) {
        UserRepresentation u = keycloak.realm(realm).users().get(id).toRepresentation();
        return UserWrapper.wrap(u);
    }

    @Override
    public GroupWrapper groupDetail(String id) {
        return GroupWrapper.wrap(keycloak.realm(realm).groups().group(id).toRepresentation());
    }

    @Override
    public void update(UserWrapper user) {
        keycloak.realm(realm).users().get(user.getId()).update(user.unwrap());
    }

    @Override
    public void disable(String id) {
        UserResource userResource = keycloak.realm(realm).users().get(id);
        UserRepresentation u = userResource.toRepresentation();
        u.setEnabled(false);
        userResource.update(u);
    }

    @Override
    public void enable(String id) {
        UserResource userResource = keycloak.realm(realm).users().get(id);
        UserRepresentation u = userResource.toRepresentation();
        u.setEnabled(true);
        userResource.update(u);
    }

    @Override
    public void resetPassword(String id) {
        CredentialRepresentation cre = new CredentialRepresentation();
        cre.setTemporary(true);
        cre.setType(CredentialRepresentation.PASSWORD);
        String password = RandomStringUtils.randomAlphanumeric(8);
        cre.setValue(password);
        UserResource userResource = keycloak.realm(realm).users().get(id);
        userResource.resetPassword(cre);
        Object[] args = {password};
        MailBean bean = new MailBean(userResource.toRepresentation().getEmail(), messageSource.getMessage("avatar.mail.resetPassword.subject"
                , null, LocaleContextHolder.getLocale())
                , messageSource.getMessage("avatar.mail.resetPassword.body", args, LocaleContextHolder.getLocale())
                , null, messageSource.getMessage("avatar.mail.from", null, LocaleContextHolder.getLocale()));
        send(bean);
    }

    @Override
    public boolean isUsernameVacant(String username) {
        //NOT VACANT
        return findByUsername(username) == null;
    }

    @Override
    public boolean isEmailVacant(String email) {
        List<UserRepresentation> lst = keycloak.realm(realm).users().search(null, null, null, email, null, null);
        if (log.isTraceEnabled()) {
            for (UserRepresentation u : lst) {
                log.trace("The email is {}", u.getEmail());
            }
        }
        if (lst.size() < 1) {
            return true;
        } else {
            for (UserRepresentation u : lst) {
                if (u.getEmail().equals(email)) return false;//NOT VACANT
            }
        }
        return true;
    }

    @Override
    public PagedListHolder<GroupRepresentation> groups(String groupName) {
        PagedListHolder page = new PagedListHolder();
        if (groupName == null) {
            List<GroupRepresentation> grps = keycloak.realm(realm).groups().groups();
            page.setSource(grps);
        } else {
            List<GroupRepresentation> grps = keycloak.realm(realm).groups().groups(groupName, 0, 1000);
            page.setSource(grps);
        }
        return page;
    }

    @Override
    public void groupDelete(String id) {
        keycloak.realm(realm).groups().group(id).remove();
    }

    @Override
    public void send(MailBean mailBean) {
        if (log.isDebugEnabled()) {
            log.debug("Mail is being send to {}", mailBean.getTo());
        }
        try {
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(mailBean);
//            if (log.isTraceEnabled()) {
//                log.trace("mail bean json string is: {}", json);
//            }
            rabbitTemplate.convertAndSend(Athena.ROUTING_KEY, json);//将json格式的字符发送到名为AvatarApplication.routingKey的队列中
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}

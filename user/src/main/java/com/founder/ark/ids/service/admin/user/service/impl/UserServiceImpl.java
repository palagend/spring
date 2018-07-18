package com.founder.ark.ids.service.admin.user.service.impl;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.admin.user.dao.UserDao;
import com.founder.ark.ids.service.admin.user.tool.MailBean;
import com.founder.ark.ids.service.admin.user.tool.MailTemplate;
import com.founder.ark.ids.service.admin.user.tool.Mailer;
import com.founder.ark.ids.service.api.admin.UserService;
import com.founder.ark.ids.service.core.bean.ConstantsLibrary;
import com.founder.ark.ids.service.core.bean.keycloak.User;
import com.founder.ark.ids.service.core.util.PollResult;
import com.founder.ark.ids.service.core.util.StringHelper;
import com.mysql.jdbc.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.founder.ark.ids.service.core.util.PollResult.EMAIL;
import static com.founder.ark.ids.service.core.util.PollResult.USERNAME;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;

/**
 * @author huyh(mailto:huyh@founder.company)
 */
@Service("kcUserServiceImpl")
public class UserServiceImpl implements UserService {
    @Autowired
    private Keycloak keycloak;
    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String company;
    @Autowired
    private UserDao dao;
    @Value("${ids.home.url: http://ids.fzyun.io}")
    private String idsHomeUrl;
    @Value("${kb.home.url: http://kb.fzyun.net}")
    private String kbHomeUrl;
    @Autowired
    private Mailer mailer;

    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public ResponseObject create(UserRepresentation user, Boolean temporary) {
        user.setEnabled(true);
        UsersResource usersRes = keycloak.realm(company).users();
        GroupsResource groupResource = keycloak.realm(company).groups();
        Response resp = usersRes.create(user);
        if (resp.getStatus() == SC_CREATED) {
            logger.info("kc新建用户成功");
            CredentialRepresentation cr = new CredentialRepresentation();
            cr.setType(CredentialRepresentation.PASSWORD);
            String pwd = StringHelper.genPassword(8);
            cr.setValue(pwd);
            cr.setTemporary(temporary);
            List<UserRepresentation> list = usersRes.search(user.getUsername());
            UserRepresentation userRepresentation = null;
            for (UserRepresentation item : list) {
                if (item.getUsername().equalsIgnoreCase(user.getUsername())) {
                    userRepresentation = item;
                    break;
                }
            }

            List<GroupRepresentation> groupRepresentationList = groupResource.groups("所有人", 0, 999);
            if (groupRepresentationList != null && groupRepresentationList.size() > 0) {
                usersRes.get(userRepresentation.getId()).joinGroup(groupRepresentationList.get(0).getId());
            }
            User idsUser = new User();
            idsUser.setId(userRepresentation.getId());
            idsUser.setEnabled(userRepresentation.isEnabled());
            idsUser.setEmail(user.getEmail());
            idsUser.setUsername(user.getUsername());
            idsUser.setMobilePhone(user.getAttributes().get("mobilePhone").get(0));
            idsUser.setFirstName(user.getLastName() + user.getFirstName());
            idsUser.setCompany(company);
            idsUser.setFederationName("IDS");
            dao.save(idsUser);
            logger.info("ids新建用户成功");
            //把重置密码放到用户保存在本地后
            usersRes.get(userRepresentation.getId()).resetPassword(cr);
            sendmail(new MailTemplate(user.getUsername(), user.getEmail(), pwd, idsHomeUrl, kbHomeUrl, MailTemplate.INITIAL_PASSWORD));
            logger.info("新建用户初始密码成功");

            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        }
        return ResponseObject.newErrorResponseObject(resp.getStatus(), resp.getStatusInfo().getReasonPhrase());
    }

    public void sendmail(MailTemplate template) {
        MailBean bean = new MailBean();
        bean.setTo(template.getEmail());
        bean.setFrom(template.getFrom());
        bean.setSubject(template.getSubject());
        bean.setBody(template.getBody());
        mailer.sendmail(bean);
    }

    @Override
    public PollResult poll(User user) {
        String mobilePhone = user.getMobilePhone();
        if ((!StringUtils.isEmptyOrWhitespaceOnly(mobilePhone)) && dao.findByMobilePhone(mobilePhone) != null) {
            return PollResult.MOBILE.setOccupied(true);
        }
        String realmName = user.getCompany() == null ? company : user.getCompany();
        logger.info("realName=" + realmName);
        List<UserRepresentation> lst;
        String username = user.getUsername();
        if (!StringUtils.isEmptyOrWhitespaceOnly(username)) {
            lst = keycloak.realm(realmName).users().search(username);
            //因为kc提供的搜索是模糊搜索，所以需要进行细致的对比，确保用户名完全一样
            for (UserRepresentation ur :
                    lst) {
                if (ur.getUsername().equalsIgnoreCase(username)) {
                    return USERNAME.setOccupied(true);
                }
            }
        }
        String email = user.getEmail();
        if (!StringUtils.isEmptyOrWhitespaceOnly(email)) {
            lst = keycloak.realm(realmName).users().search(email, 0, 1000);
            for (UserRepresentation ur :
                    lst) {
                if (ur.getEmail().equalsIgnoreCase(email)) {
                    return EMAIL.setOccupied(true);
                }
            }
        }
        return PollResult.VACANT;
    }

    @Override
    public void update(UserRepresentation user) {
        UserResource userResource = keycloak.realm(company).users().get(user.getId());
        UserRepresentation rep = userResource.toRepresentation();
        List<String> mobilePhone = user.getAttributes().get("mobilePhone");
        if (mobilePhone.get(0) != null) {
            rep.getAttributes().put("mobilePhone", mobilePhone);
        }
        if (user.getEmail() != null) {
            rep.setEmail(user.getEmail());
        }
        if (user.getFirstName() != null) {
            rep.setFirstName(user.getFirstName());
            rep.setLastName(user.getLastName());
        }
        userResource.update(rep);
    }

    @Override
    public UserRepresentation getUserInfo(String id) {
        return keycloak.realm(company).users().get(id).toRepresentation();
    }

    //删除用户
    @Override
    public Response delete(String id) {
        return keycloak.realm(company).users().delete(id);
    }

    //批量删除用户
    @Override
    public void batchDeleteUsers(List<String> ids) {
        for (String id : ids) {
            keycloak.realm(company).users().delete(id);
        }
    }

    //重置用户密码

    @Override
    public void resetPassword(String id) {
        String randomPassword = StringHelper.genPassword(8);
        CredentialRepresentation newCredentialRepresentation = new CredentialRepresentation();
        //首先需要设置资格代表的类型，这里想要修改密码，因此设置Type为PASSWORD
        newCredentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        //设置密码的值
        newCredentialRepresentation.setValue(randomPassword);
        //设置是否是临时密码，该值如果为true，则用户在首次使用新密码登录后需要再设置一个新的密码
        newCredentialRepresentation.setTemporary(false);
        UserResource userResource = keycloak.realm(company).users().get(id);
        userResource.resetPassword(newCredentialRepresentation);
        sendmail(new MailTemplate(userResource.toRepresentation().getUsername(), userResource.toRepresentation().getEmail(), randomPassword, idsHomeUrl, kbHomeUrl, MailTemplate.RESET_PASSWORD));
    }

    //批量禁用/激活用户
    @Override
    public void batchSwitch(List<String> ids, Boolean bool) {
        for (String id : ids) {
            //设置每个用户的激活禁用状态其实是将user的enabled属性置为true或false
            UserResource userResource = keycloak.realm(company).users().get(id);
            UserRepresentation userRepresentation = userResource.toRepresentation();
            userRepresentation.setEnabled(bool);
            userResource.update(userRepresentation);
            User e = dao.findOne(id);
            e.setEnabled(bool);
            dao.save(e);
            userResource.logout();
        }
    }

    @Override
    public List<UserRepresentation> users(Integer pageNumber, Integer pageSize) {
        return keycloak.realm(company).users().list(pageNumber == null ? 0 : pageNumber - 1, pageSize == null ? 20 : pageSize);
    }

    //获取所有用户信息
    @Override
    public PageData<User> users(Integer pageNumber, Integer pageSize, String searchString) {
        Page<User> page;
        Sort sort = new Sort("username");
        PageRequest pr = new PageRequest(pageNumber - 1, pageSize, sort);
        page = searchString == null ? dao.findAllInTheSameCompany(company, pr) : dao.searchUsers(searchString, company, pr);
        boolean recapture = true;
        List<User> rowsContents = new ArrayList<>(pageSize);
        while (recapture) {
            page = searchString == null ? dao.findAllInTheSameCompany(company, pr) : dao.searchUsers(searchString, company, pr);
            recapture = false;
            rowsContents = page.getContent();
            for (User rowContent : rowsContents) {
                //通过kc的接口获取每一个特定User所属的组
                List<GroupRepresentation> groupsReps = null;
                try {
                    groupsReps = keycloak.realm(company).users().get(rowContent.getId()).groups();
                    List<String> groups = new ArrayList<>();
                    for (GroupRepresentation groupRep : groupsReps) {
                        groups.add(groupRep.getName());
                    }
                    rowContent.setGroups(groups);
                } catch (NotFoundException e) {
                    logger.info("获取 "+rowContent.getUsername()+" 所属组失败，此用户不存在，删除此用户。");
                    dao.delete(rowContent.getId());
                    recapture = true;
                    break;
                }
            }
        }

        PageData<User> pageData = new PageData<>();
        pageData.setPageSize(page.getSize());
        pageData.setPageNumber(page.getNumber() + 1);
        pageData.setTotal(page.getTotalElements());
        pageData.setRows(rowsContents);
        return pageData;
    }

    @Override
    public PageData<User> simpleLocalUser() {
        Page<User> page;
        Sort sort = new Sort("username");
        PageRequest pr = new PageRequest(0, 9999, sort);
        page = dao.findAll(pr);
        PageData<User> pageData = new PageData<>();
        pageData.setPageSize(9999);
        pageData.setPageNumber(1);
        pageData.setTotal(page.getTotalElements());
        pageData.setRows(page.getContent());
        return pageData;
    }

}

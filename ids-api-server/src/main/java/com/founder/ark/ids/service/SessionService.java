package com.founder.ark.ids.service;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.bean.keycloak.User;
import org.keycloak.representations.idm.UserSessionRepresentation;

import java.util.List;

public interface SessionService {
    //获取所有在线用户
    PageData<User> getOnlineUsers();

    //判断指定用户里面有多少用户在线
    List<User> getOnlineUsersInDesignatedUsers(List<User> designatedUsers);

    //注销会话-单条注销
    ResponseObject deleteSession(String id);

    //获取一个用户的所有session信息
    List<UserSessionRepresentation> getUserSessions(String id);

    //登出一个用户的所有会话信息（注销用户）
    ResponseObject userLogout(String id);

    //批量注销用户的所有会话信息
    ResponseObject userBatchLogout(List<String> ids);
}

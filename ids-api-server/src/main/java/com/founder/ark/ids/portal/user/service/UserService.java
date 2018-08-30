package com.founder.ark.ids.portal.user.service;

import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.bean.keycloak.Client;
import com.founder.ark.ids.bean.keycloak.User;

import java.util.List;
import java.util.Map;

/**
 * Created by cheng.ly on 2018/3/21.
 */
public interface UserService {

    User getUserInfo(String id);

    ResponseObject updatePassword(String id, String oldPassword, String newPassword, String confirmPassword);

    List<Client> getClientOfUser(String id);

    Map obtainUserId(String email);

}

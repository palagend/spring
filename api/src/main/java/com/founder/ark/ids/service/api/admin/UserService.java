package com.founder.ark.ids.service.api.admin;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.core.bean.keycloak.User;
import com.founder.ark.ids.service.core.util.PollResult;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author huyh (mailto:huyh@founder.com).
 */

/**
 * 该接口类用于定义基于Keycloak或者自定义的用户操作，
 * 实现自定义的用户操作是由于Keycloak没有原生的支持.
 */
public interface UserService {
    /**
     * 以下是已实现的接口
     */

    //新增用户
    ResponseObject create(UserRepresentation user, Boolean temporary);

    //查询某个用户是否已经存在
    PollResult poll(User user);

    //修改用户信息
    void update(UserRepresentation user);

    //删除用户
    Response delete(String id);

    //批量删除用户
    void batchDeleteUsers(List<String> ids);

    //查看用户信息
    UserRepresentation getUserInfo(String id);

    //获取所有用户信息，目前还不支持分页
    List<UserRepresentation> users(Integer pageNumber, Integer pageSize);

    //重置用户密码
    void resetPassword(String id);

    //批量禁用/禁用用户，bool为true代表激活，bool为false代表禁用
    void batchSwitch(List<String> ids, Boolean bool);

    PageData<User> users(Integer pageNumber, Integer pageSize, String searchString);

    PageData<User>  simpleLocalUser();
//    ResponseObject updatePassword(String id, String newPassword);
}

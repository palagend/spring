package com.founder.ark.ids.service.api.admin;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.core.bean.keycloak.Client;
import com.founder.ark.ids.service.core.bean.keycloak.Group;
import com.founder.ark.ids.service.core.util.PollResult;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * 该接口类用于定义基于keycloak的相关组操作
 */

public interface GroupService {

    //获取所有组信息
    PageData<Group> getGroups(Integer pageNumber, Integer pageSize, String groupName);

    //查看组信息
    GroupRepresentation getGroupInfo(String id);

    //删除组
    void deleteGroup(String id);

    //批量删除组
    void batchDeleteGroups(List<String> ids);

    //新增组
    ResponseObject createOrUpdate(Group group);

    //组的唯一性校验接口
    PollResult poll(Group group);

    //查看组内成员接口
    PageData usersInGroup(String id);

    //查看组外成员
    PageData<UserRepresentation> usersNotInGroup(String id);

    /**组内应用*/
    PageData<Client> innerClients(String id);

    /**组外应用*/
    PageData<Client> externalClients(String id);
}

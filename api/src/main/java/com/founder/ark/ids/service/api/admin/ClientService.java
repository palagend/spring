package com.founder.ark.ids.service.api.admin;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.core.bean.keycloak.Client;
import com.founder.ark.ids.service.core.bean.keycloak.Group;
import com.founder.ark.ids.service.core.util.PollResult;

import java.util.List;

/**
 * 该接口类用于定义基于Keycloak或者自定义的应用操作，
 * 实现自定义的应用操作是由于Keycloak没有原生的支持
 */
public interface ClientService {

    void delete(String id);

    void batchDeleteGroups(List<String> ids);

    ResponseObject updateAssigendGroups(List<String> ids, String appId);

    List<Group> getAssignedGroups(String id);

    List<Group> getUnassignedGroups(String id);

    PageData<Client> getClients(Integer pageNumber, Integer pageSize, String appName);

    ResponseObject createOrUpdate(Client client);

    Client getConfigOfClient(String id,boolean allInfo);
    //应用的唯一性校验接口
    PollResult poll(Client client);
}

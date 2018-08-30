package com.founder.ark.ids.admin.service.impl;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.admin.dao.ClientDao;
import com.founder.ark.ids.admin.dao.GroupDao;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.bean.keycloak.Client;
import com.founder.ark.ids.bean.keycloak.Group;
import com.founder.ark.ids.service.ClientService;
import com.founder.ark.ids.util.PollResult;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;

@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private Keycloak keycloak;
    private static final String IDS_VIEW_PROFILE = "ids-view-profile";
    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String defaultCompany;


    @Autowired
    public ClientServiceImpl(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @Autowired
    private ClientDao clientDao;
    @Autowired
    private GroupDao groupDao;

    private static Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    /**
     * 删除应用
     */
    @Override
    public void delete(String id) {
        logger.info("删除应用。");
        clientDao.deleteById(id);
        ClientsResource clientResource = keycloak.realm(defaultCompany).clients();
        clientResource.get(id).remove();
    }

    /**
     * 批量删除应用
     */
    @Override
    public void batchDeleteGroups(List<String> ids) {
        logger.info("批量删除应用。");
        ClientsResource clientResource = keycloak.realm(defaultCompany).clients();
        for (String id : ids) {
            clientDao.deleteById(id);
            clientResource.get(id).remove();
        }
    }

    /**
     * 更新应用已分配的组信息
     */
    @Override
    public ResponseObject updateAssigendGroups(List<String> ids, String appId) {
        logger.info("更新应用已分配的组信息");
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        List<Group> groups = groupDao.findAll();
        //首先重置组被分配的应用角色
        for (Group group :
                groups) {
            //获取指定组对应指定客户端的角色信息。
            RoleScopeResource roleScopeResource = groupsResource.group(group.getId()).roles().clientLevel(appId);
            List<RoleRepresentation> effectiveRole = roleScopeResource.listEffective();
            List<RoleRepresentation> viewRoleList = new ArrayList<>(1);
            for (RoleRepresentation var1 :
                    effectiveRole) {
                if (IDS_VIEW_PROFILE.equals(var1.getName())) {
                    viewRoleList.add(var1);
                    roleScopeResource.remove(viewRoleList);
                    break;
                }
            }
        }
        //根据传参中的组Id重新建立与Client之间的角色映射
        for (String groupId :
                ids) {
            RoleScopeResource roleScopeResource = groupsResource.group(groupId).roles().clientLevel(appId);
            List<RoleRepresentation> availableRole = roleScopeResource.listAvailable();
            List<RoleRepresentation> viewRoleList = new ArrayList<>(1);
            for (RoleRepresentation var2 :
                    availableRole) {
                if (IDS_VIEW_PROFILE.equals(var2.getName())) {
                    viewRoleList.add(var2);
                    roleScopeResource.add(viewRoleList);
                    break;
                }
            }
        }

        return ResponseObject.newSuccessResponseObject(null, "update succeed");
    }

    /**
     * 新建应用或更新应用
     */
    @Override
    public ResponseObject createOrUpdate(Client client) {
        ClientsResource clientsResource = keycloak.realm(defaultCompany).clients();
        ClientRepresentation cr = new ClientRepresentation();
        String appName = client.getAppName();
        cr.setClientId(appName);
        cr.setEnabled(true);
        cr.setProtocol("openid-connect");
        cr.setRootUrl(client.getLoginUrl());
        //根据ClientId是否为空来执行更新创建操作
        if (client.getId() == null) {
            logger.info("更新应用： " + appName);
            //校验Keycloak未录入数据库的Client名称是否已经占用
            List<ClientRepresentation> tempList = clientsResource.findByClientId(appName);
            if (tempList != null && tempList.size() > 0) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.CLIENT_NAME_OCCUPIED, ConstantsLibrary.Message.CLIENT_NAME_OCCUPIED);
            }
            Response response = clientsResource.create(cr);
            if (response.getStatus() == SC_CREATED) {
                String clientId = clientsResource.findByClientId(appName).get(0).getId();
                createIdsViewRole(clientId);
                client.setId(clientId);
                client.setCreatedTimestamp(System.currentTimeMillis());
                clientDao.save(client);
                return ResponseObject.newSuccessResponseObject(null, "create succeed");
            } else if (response.getStatus() == SC_CONFLICT) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.CLIENT_NAME_OCCUPIED, ConstantsLibrary.Message.CLIENT_NAME_OCCUPIED);
            } else {
                return ResponseObject.newErrorResponseObject(-1, response.getStatusInfo().getReasonPhrase());
            }
        } else {
            logger.info("创建应用： " + appName);
            cr.setId(client.getId());
            clientsResource.get(client.getId()).update(cr);
            clientDao.save(client);
            return ResponseObject.newSuccessResponseObject(null, "update succeed");
        }
    }

    /**
     * 获取应用列表
     */
    @Override
    public PageData<Client> getClients(Integer pageNumber, Integer pageSize, String appName) {
        logger.info("获取应用列表");
        PageRequest pageRequest = new PageRequest(pageNumber - 1, pageSize);
        Page<Client> page;
        //根据appName是否为空获取全部应用或者查询
        page = appName == null ? clientDao.findAll(pageRequest) : clientDao.searchClientByName(appName, pageRequest);
        PageData pageData = new PageData();
        pageData.setPageSize(page.getSize());
        pageData.setPageNumber(page.getNumber() + 1);
        pageData.setTotal(page.getTotalElements());
        pageData.setRows(page.getContent());
        return pageData;
    }


    /**
     * 获取应用分配的组信息
     */
    @Override
    public List<Group> getAssignedGroups(String id) {
        List<Group> idsGroups = groupDao.findAll();
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        Iterator<Group> iterator = idsGroups.iterator();
        List<Group> assignedGroups = new ArrayList<>();
        while (iterator.hasNext()) {
            Group group = iterator.next();
            RoleScopeResource roleScopeResource = groupsResource.group(group.getId()).roles().clientLevel(id);
            List<RoleRepresentation> effectiveRole = roleScopeResource.listEffective();
            for (RoleRepresentation var2 :
                    effectiveRole) {
                if (IDS_VIEW_PROFILE.equals(var2.getName())) {
                    assignedGroups.add(group);
                    break;
                }
            }
        }
        return assignedGroups;
    }

    /**
     * 获取应用未分配的组信息
     */
    @Override
    public List<Group> getUnassignedGroups(String id) {
        List<Group> idsGroups = new ArrayList<>(groupDao.findAll());
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        Iterator<Group> iterator = idsGroups.iterator();
        while (iterator.hasNext()) {
            Group group = iterator.next();
            RoleScopeResource roleScopeResource = groupsResource.group(group.getId()).roles().clientLevel(id);
            List<RoleRepresentation> effectiveRole = roleScopeResource.listEffective();
            for (RoleRepresentation var2 :
                    effectiveRole) {
                if (IDS_VIEW_PROFILE.equals(var2.getName())) {
                    iterator.remove();
                    break;
                }
            }
        }
        return idsGroups;
    }

    /**
     * 获取应用配置信息
     */
    @Override
    public Client getConfigOfClient(String id, boolean allInfo) {
        Optional<Client> client = clientDao.findById(id);
        //判断allInfo，决定是否在返回信息中加入已经分配的组信息
        if (allInfo && client.isPresent()) {
            List<Group> assignedGrooups = getAssignedGroups(id);
            client.get().setAssignedGroups(assignedGrooups);
            return client.get();
        } else return null;
    }

    /**
     * 新建的应用将默认创建ids-view-proflie角色
     */
    public void createIdsViewRole(String clientId) {
        ClientsResource clientsResource = keycloak.realm(defaultCompany).clients();
        RoleRepresentation roleRepresentation = new RoleRepresentation(IDS_VIEW_PROFILE, "一旦用户或组被分配此角色，则具有访问本客户端的权限。", true);
        roleRepresentation.setClientRole(true);
        clientsResource.get(clientId).roles().create(roleRepresentation);
    }

    /**
     * 应用的唯一性校验接口
     */
    @Override
    public PollResult poll(Client client) {
        Client searchedClient = clientDao.findByAppName(client.getAppName());
        if (searchedClient != null) {
            return PollResult.CLIENTNAME.setOccupied(true);
        } else {
            return PollResult.VACANT;
        }
    }
}

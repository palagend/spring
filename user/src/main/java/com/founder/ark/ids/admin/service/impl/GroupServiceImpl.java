package com.founder.ark.ids.admin.service.impl;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.admin.dao.ClientDao;
import com.founder.ark.ids.admin.dao.GroupDao;
import com.founder.ark.ids.admin.dao.UserDao;
import com.founder.ark.ids.bean.keycloak.Client;
import com.founder.ark.ids.bean.keycloak.Group;
import com.founder.ark.ids.bean.keycloak.User;
import com.founder.ark.ids.service.GroupService;
import com.founder.ark.ids.util.PollResult;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.ClientMappingsRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.founder.ark.ids.util.PollResult.GROUPNAME;

@Service
public class GroupServiceImpl implements GroupService {
    private static final String ALL_APPS = "allApps";
    private static final String IDS_VIEW_PROFILE = "ids-view-profile";
    @Autowired
    private Keycloak keycloak;
    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String defaultCompany;


    @Autowired
    public GroupServiceImpl(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupDao groupDao;
    @Autowired
    private ClientDao clientDao;


    private static Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);


    //获取所有组信息
    @Override
    public PageData<Group> getGroups(Integer pageNumber, Integer pageSize, String groupName) {
        Page<Group> page;
        Sort sort = new Sort(Sort.Direction.DESC, "createdTimestamp");
        PageRequest pr = new PageRequest(pageNumber - 1, pageSize, sort);
        //从本地数据库读取
        if (groupName == null) {
            logger.info("列表展示所有组信息");
            page = groupDao.findAll(pr);
        } else {
            logger.info("列表展示检索组信息");
            page = groupDao.searchGroups(groupName, pr);
        }
        PageData<Group> pageData = new PageData<>();
        pageData.setPageSize(page.getSize());
        pageData.setPageNumber(page.getNumber() + 1);
        pageData.setTotal(page.getTotalElements());
        pageData.setRows(page.getContent());
        return pageData;
    }

    @Override
    public GroupRepresentation getGroupInfo(String id) {
        //调用KeycloakAPI获取完整组信息
        logger.info("获取组信息");
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        return groupsResource.group(id).toRepresentation();
    }


    /**
     * 删除组
     */
    @Override
    public void deleteGroup(String id) {
        logger.info("删除组：" + id);
        //删除Keycloak组
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        //删除本地数据库组
        groupsResource.group(id).remove();
        groupDao.deleteById(id);
    }

    /**
     * 批量删除组
     */
    @Override
    public void batchDeleteGroups(List<String> ids) {
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        for (String id : ids) {
            groupsResource.group(id).remove();
        }
    }

    /**
     * 新建组或更新组
     */
    @Override
    public ResponseObject createOrUpdate(Group group) {
        // TODO: 2018/8/27
        return ResponseObject.newErrorResponseObject(-1,"TODO");
        /*GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        UsersResource usersResource = keycloak.realm(defaultCompany).users();
        GroupRepresentation groupRep = new GroupRepresentation();
        Map<String, List<String>> map = parseGroupMap(group);
        groupRep.setAttributes(map);
        groupRep.setName(group.getGroupName());
        //groupId不为空，执行更新操作
        if (group.getId() != null) {
            logger.info("对组 " + group.getGroupName() + " 执行更新操作。");
            String updateId = group.getId();
            groupRep.setId(updateId);
            String sql = "SELECT ugm.USER_ID FROM USER_GROUP_MEMBERSHIP ugm WHERE GROUP_ID = '" + updateId + "'";
            PageData<User> data = new PageData<>();
            List<String> userIds = KeycloakDataSourceDao.getJdbcOperations().queryForList(sql, String.class);
            //限制对所有人组的成员进行修改
            if (!groupDao.findOne(updateId).getGroupType().equals("everyone")) {
                logger.info("将" + userIds.size() + "个用户移出组" + group.getGroupName());
                for (String var :
                        userIds) {
                    String deleteSql = "DELETE FROM `USER_GROUP_MEMBERSHIP` WHERE (`GROUP_ID`='" + updateId + "') AND (`USER_ID`='" + var + "')";
                    KeycloakDataSourceDao.getJdbcOperations().execute(deleteSql);
                }
                logger.info("将" + group.getUsers().length + "个用户加入组" + group.getGroupName());
                for (String var :
                        group.getUsers()) {
                    String addSql = "INSERT INTO USER_GROUP_MEMBERSHIP VALUES('" + updateId + "','" + var + "')";
                    KeycloakDataSourceDao.getJdbcOperations().execute(addSql);
                }
                keycloak.realm(defaultCompany).clearUserCache();
            }
            try {
                groupsResource.group(updateId).update(groupRep);
                //如果更新成功，则同时更新IDS的数据库
                //首先获取旧的group的信息
                Group oldGroup = groupDao.getOne(group.getId());
                //获取更新的组的当前时间戳
                long updatedTimeStamp = System.currentTimeMillis();
                group.setUpdatedTimestamp(updatedTimeStamp);
                //因为上传的新的groupJson信息不包含createdTimestamp，所以把旧的createdTimestamp赋值给新的
                group.setCreatedTimestamp(oldGroup.getCreatedTimestamp());
                //重置组对应的clientMapping信息
                resetMapping(updateId);
                //重新建立组与客户端之间的角色映射
                mappingRole(group);
                //把新创建的组保存在IDS数据库里,同理，将apps和users置为null
                group.setUsers(null);
                group.setApps(null);
                groupDao.save(group);
                return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
            } catch (Exception e) {
                logger.error("createOrUpdate Exception", e);
                return ResponseObject.newErrorResponseObject(1107, "update failed");
            }

        } else {
            //groupId为空，执行新建操作
            Response resp = groupsResource.add(groupRep);
            if (resp.getStatus() == SC_CREATED) {
                logger.info("新建组： " + group.getGroupName());
                List<GroupRepresentation> list = groupsResource.groups(groupRep.getName(), 0, 1);
                for (GroupRepresentation var :
                        list) {
                    group.setId(var.getId());
                    if (group.getApps() != null) {
                        mappingRole(group);
                    }

                    if (var.getName().equals(groupRep.getName())) {
                        //查找到了在kc新创建的组返回的组id
                        group.setId(var.getId());
                        //获取新创建的组的当前时间戳
                        long createdTimeStamp = System.currentTimeMillis();
                        group.setCreatedTimestamp(createdTimeStamp);
                        //把新创建的组保存在IDS数据库里
                        for (String userId :
                                group.getUsers()) {
                            String addSql = "INSERT INTO USER_GROUP_MEMBERSHIP VALUES('" + var.getId() + "','" + userId + "')";
                            KeycloakDataSourceDao.getJdbcOperations().execute(addSql);
                        }
                        keycloak.realm(defaultCompany).clearUserCache();
                        //将group的apps和users置为空后在存入本地数据库
                        group.setApps(null);
                        group.setUsers(null);
                        groupDao.save(group);
                    }
                }
                return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
            } else if (resp.getStatus() == SC_CONFLICT) {
                return ResponseObject.newErrorResponseObject(1106, ConstantsLibrary.Message.GROUPNAME_OCCUPIED);
            } else {
                return ResponseObject.newErrorResponseObject(resp.getStatus(), resp.getStatusInfo().getReasonPhrase());
            }
        }
    */}


    /**
     * 组内用户
     */
    @Override
    public PageData usersInGroup(String id) {
        // TODO: 2018/8/27
        return null;
        /*String sql = "SELECT ugm.USER_ID FROM USER_GROUP_MEMBERSHIP ugm WHERE GROUP_ID = '" + id + "'";
        PageData<User> data = new PageData<>();
        List<String> userIds = KeycloakDataSourceDao.getJdbcOperations().queryForList(sql, String.class);
        List<User> users = new ArrayList<>(userIds.size() + 1);
        logger.info("获取组内用户数：" + userIds.size());
        for (String userId :
                userIds) {
            User user = new User();
            user.setId(userId);
            users.add(user);
        }
        data.setRows(users);
        data.setPageNumber(1);
        data.setPageSize(9999);
        data.setTotal(users.size());
        return data;
    */}

    /**
     * 组外用户
     */
    @Override
    public PageData usersNotInGroup(String id) {
        logger.info("查找组外用户。");
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        Sort sort = new Sort("username");
        List<User> externalUsers;
        List<User> innerUsers = new ArrayList<>();
        //获取到所有用户后，过滤掉组内的成员，即为组外用户
        for (UserRepresentation userRep :
                groupsResource.group(id).members()) {
            User user = new User();
            user.setId(userRep.getId());
            innerUsers.add(user);
        }
        //调用用户管理的service，获取简略属性的本地用户
        externalUsers = userDao.findAll(sort);
        for (User var1 :
                innerUsers) {
            externalUsers.removeIf(user -> user.getId().equals(var1.getId()));
        }
        PageData<User> data = new PageData<>();
        data.setRows(externalUsers);
        data.setPageNumber(1);
        data.setPageSize(externalUsers.size());
        data.setTotal(externalUsers.size());
        return data;
    }

    /**
     * 组内应用
     */
    @Override
    public PageData innerClients(String id) {
        logger.info("获取组内应用。");
        PageData<Client> data = new PageData<>();
        List<Client> mappingClients = getMappingClients(id);
        data.setRows(mappingClients);
        data.setPageNumber(1);
        data.setPageSize(mappingClients.size());
        data.setTotal(mappingClients.size());
        return data;
    }

    /**
     * 组外应用
     */
    @Override
    public PageData externalClients(String id) {
        logger.info("获取组外应用。");
        PageData<Client> data = new PageData<>();
        List<Client> listClients = clientDao.findAll();
        //新建组时获取所有应用信息，不再操作listClientsRep
        if (!ALL_APPS.equals(id)) {
            for (Client var :
                    getMappingClients(id)) {
                listClients.removeIf(var2 -> var.getId().equals(var2.getId()));
            }
        }
        data.setRows(listClients);
        data.setPageNumber(1);
        data.setPageSize(listClients.size());
        data.setTotal(listClients.size());
        return data;
    }


    /**
     * 唯一性校验接口
     */
    @Override
    public PollResult poll(Group group) {
        String realmName = group.getCompany() == null ? defaultCompany : group.getCompany();
        //group接口未提供全部返回的信息，必须制定返回数量，此处暂设为9999，假定一个公司的组的数量不会超过9999。。。

        List<GroupRepresentation> lst = keycloak.realm(realmName).groups().groups(group.getGroupName(), 0, 9999);
        for (GroupRepresentation gr :
                lst) {
            if (gr.getName().equals(group.getGroupName())) {
                return GROUPNAME.setOccupied(true);
            }
        }
        return PollResult.VACANT;
    }


    /**
     * 对组信息进行转换
     */
    private Map<String, List<String>> parseGroupMap(Group group) {
        Map<String, List<String>> map = new HashMap<>(2);
        if (group.getGroupDescription() != null) {
            map.put("groupDescription", Collections.singletonList(group.getGroupDescription()));
        } else {
            map.put("groupDescription", Collections.singletonList("未定义的组描述"));
        }
        if (group.getGroupType() != null) {
            map.put("groupType", Collections.singletonList(group.getGroupType()));
        } else {
            map.put("groupType", Collections.singletonList("normal"));
        }
        return map;
    }

    /**
     * 对组映射的应用信息进行转换
     */
    private List<Client> getMappingClients(String id) {
        //获取指定组的全部clientMapping
        Map<String, ClientMappingsRepresentation> map = keycloak.realm(defaultCompany).groups().group(id).roles().getAll().getClientMappings();
        List<Client> listInner = new ArrayList<>();
        //遍历组的clientMapping，如果存在ids-view-profile，认为该应用被分配给组
        if (map != null) {
            for (String str : map.keySet()) {
                ClientMappingsRepresentation clientMappingsRepresentation = map.get(str);
                //只获取本地数据库录入的client
                if (clientDao.findById(clientMappingsRepresentation.getId()).isPresent()) {
                    for (RoleRepresentation roleRepresentation : clientMappingsRepresentation.getMappings()) {
                        if (roleRepresentation.getName().equals(IDS_VIEW_PROFILE)) {
                            listInner.add(new Client(clientMappingsRepresentation.getId(), clientMappingsRepresentation.getClient()));
                        }
                    }
                }
            }
        }
        return listInner;
    }

    /**
     * 新建或更新时建立client与group的clientMapping，以授予group对指定client的访问权限。
     */
    private void mappingRole(Group group) {
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        ClientsResource clientsResource = keycloak.realm(defaultCompany).clients();
        String groupId = group.getId();
        //遍历传参中的client
        for (int i = 0; i < group.getApps().length; i++) {
            String clientId = group.getApps()[i];
            //获取特定组对应特定客户端的角色信息
            RoleScopeResource roleScopeResource = groupsResource.group(groupId).roles().clientLevel(clientId);
            List<RoleRepresentation> effectiveRole = roleScopeResource.listEffective();
            List<RoleRepresentation> availableRole = roleScopeResource.listAvailable();
            //如果client没有指定的ids-view-profile,则会创建一个此角色并分配给组
            boolean shouldCreateRole = true;
            boolean roleEffective = false;
            for (RoleRepresentation var1 :
                    effectiveRole) {
                if (IDS_VIEW_PROFILE.equals(var1.getName())) {
                    shouldCreateRole = false;
                    roleEffective = true;
                    break;
                }
            }
            //当client的有效角色中没有ids-view-profile，就会继续检索client的availableRole。
            if (!roleEffective) {
                shouldCreateRole = addViewClientRole(availableRole, roleScopeResource);
            }
            //
            if (shouldCreateRole) {
                RoleRepresentation roleRepresentation = new RoleRepresentation(IDS_VIEW_PROFILE, "一旦用户或组被分配此角色，则具有访问本客户端的权限。", true);
                roleRepresentation.setClientRole(true);
                clientsResource.get(clientId).roles().create(roleRepresentation);
                availableRole = roleScopeResource.listAvailable();
                addViewClientRole(availableRole, roleScopeResource);
            }
        }
    }

    /**
     * 在group的roleMapping中添加具有访问客户端权限的角色
     */
    private boolean addViewClientRole(List<RoleRepresentation> availableRole, RoleScopeResource roleScopeResource) {
        boolean shouldCreateRole = true;
        List<RoleRepresentation> viewRoleList = new ArrayList<>(1);
        for (RoleRepresentation var2 :
                availableRole) {
            if (IDS_VIEW_PROFILE.equals(var2.getName())) {
                shouldCreateRole = false;
                viewRoleList.add(var2);
                roleScopeResource.add(viewRoleList);
                break;
            }
        }
        return shouldCreateRole;
    }

    /**
     * 重置group的有效客户端角色映射
     */
    public void resetMapping(String groupId) {
        GroupsResource groupsResource = keycloak.realm(defaultCompany).groups();
        Map<String, ClientMappingsRepresentation> map = groupsResource.group(groupId).roles().getAll().getClientMappings();
        if (map != null) {
            for (String str :
                    map.keySet()) {
                String clientId = map.get(str).getId();
                if (clientDao.findById(clientId).isPresent()) {
                    RoleScopeResource roleScopeResource = groupsResource.group(groupId).roles().clientLevel(clientId);
                    List<RoleRepresentation> effectiveRole = roleScopeResource.listEffective();
                    List<RoleRepresentation> viewRoleList = new ArrayList<>(1);
                    for (RoleRepresentation var2 :
                            effectiveRole) {
                        if (IDS_VIEW_PROFILE.equals(var2.getName())) {
                            viewRoleList.add(var2);
                            roleScopeResource.remove(viewRoleList);
                            break;
                        }
                    }
                }
            }
        }
    }

}

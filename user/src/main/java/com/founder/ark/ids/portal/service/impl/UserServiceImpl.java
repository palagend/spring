package com.founder.ark.ids.portal.service.impl;

import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.admin.dao.ClientDao;
import com.founder.ark.ids.admin.dao.UserDao;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.bean.keycloak.Client;
import com.founder.ark.ids.bean.keycloak.User;
import com.founder.ark.ids.portal.user.service.UserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by cheng.ly on 2018/3/21.
 */
@Service
public class UserServiceImpl implements UserService {
    private final Keycloak keycloak;
    private static final String IDS_VIEW_PROFILE = "ids-view-profile";
    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String company;
    @Value("${ids.keycloak.serverUrl}")
    private String serverUrl;
    @Autowired
    UserDao userDao;
    @Autowired
    ClientDao clientDao;
    @Autowired
    JedisPool jedisPool;

    private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(Keycloak keycloak) {
        this.keycloak = keycloak;
    }


    /**获取用户个人信息*/
    @Override
    public User getUserInfo(String id) {
        return userDao.findById(id).get();
    }

    /**更新密码*/
    @Override
    public ResponseObject updatePassword(String id, String oldPassword, String newPassword, String confirmPassword) {
        User user = getUserInfo(id);
        if(!newPassword.equals(confirmPassword)){
            logger.info("新密码和确认密码不一致。");
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.TWO_INPUTTED_CONFLICT, ConstantsLibrary.Message.TWO_INPUTTED_CONFLICT);
        }
        String userName = user.getUsername();
        try {
            logger.info("校验旧密码的正确性。");
            Keycloak.getInstance(serverUrl, company, userName, oldPassword, "admin-cli").tokenManager().getAccessToken();
        } catch (Exception e) {
            logger.info("密码校验失败。");
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.OLD_PASSWORD_INCORRECT,ConstantsLibrary.Message.OLD_PASSWORD_INCORRECT );
        }
        CredentialRepresentation cr = new CredentialRepresentation();
        cr.setAlgorithm("pbkdf2-sha256");
        cr.setTemporary(false);
        cr.setType("password");
        cr.setValue(newPassword);
        //重置密码
        keycloak.realm(company).users().get(id).resetPassword(cr);
        return ResponseObject.newSuccessResponseObject(null,"Success");
    }


    /**从Token获取用户邮箱*/
    private String getUserEmailFromToken(String founderAuthToken) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis.hgetAll(founderAuthToken) != null) {
                return jedis.hget(founderAuthToken,"email");
            }
        } catch (TimeoutException e) {
            logger.info("redis exception", e);
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return null;
    }

    /**获取用户已分配的应用信息*/
    @Override
    public List<Client> getClientOfUser(String id) {
        List<Client> clientsOfUsers = new ArrayList<>();
        UserResource userResource = keycloak.realm(company).users().get(id);
        Boolean shouldCheckGroup = true;
        List<Client> allClients = clientDao.findAll();
        //获取个人分配的应用信息
        for (Client var1:
             allClients) {
            List<RoleRepresentation> allRolesOfUser =  userResource.roles().clientLevel(var1.getId()).listEffective();
            if(allRolesOfUser!=null){
                for (RoleRepresentation var2:
                     allRolesOfUser) {
                    if(var2.getName().equals(IDS_VIEW_PROFILE)){
                        shouldCheckGroup = false;
                        clientsOfUsers.add(var1);
                    }
                    break;
                }
            }
            //获取所属组分配的应用信息
            if(shouldCheckGroup){
                List<GroupRepresentation> allGroups = userResource.groups();
                for (GroupRepresentation var3:
                     allGroups) {
                    List<RoleRepresentation> allRolesOfGroup = keycloak.realm(company).groups().group(var3.getId()).roles().clientLevel(var1.getId()).listEffective();
                    if(allRolesOfGroup!=null){
                        for (RoleRepresentation var2:
                                allRolesOfGroup) {
                            if(var2.getName().equals(IDS_VIEW_PROFILE)){
                                clientsOfUsers.add(var1);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return clientsOfUsers;
    }

    /**获取用户id*/
    @Override
    public Map obtainUserId(String authToken) {
        String lowerEmail = getUserEmailFromToken(authToken).toLowerCase();
        User user = userDao.findUserByEmail(lowerEmail);
        Map<String,String> map = new HashMap<>();
        if(user!=null){
            map.put("id",user.getId());
        }
        return map;
    }
}

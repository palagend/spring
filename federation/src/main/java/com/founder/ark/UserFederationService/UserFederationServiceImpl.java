package com.founder.ark.UserFederationService;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.founder.ark.Dao.LocalLDAPAuthenticationInfoDao;
import com.founder.ark.Dao.UserDao;
import com.founder.ark.ModuleClass.AuthenticationSource;
import com.founder.ark.ModuleClass.LocalLDAPAuthenticationInfo;
import com.founder.ark.UserFederationInterface.UserFederationService;
import com.founder.ark.Utilities.ConstantsLibrary;
import com.founder.ark.common.utils.StringUtil;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.core.bean.keycloak.User;
import com.founder.ark.support.task.api.JobApi;
import com.founder.ark.support.task.bean.JobBean4Add;
import com.founder.ark.support.task.bean.JobBean4Update;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ComponentResource;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.SynchronizationResultRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.ws.rs.core.Response;
import java.util.regex.Pattern;

import static org.jboss.resteasy.util.HttpResponseCodes.SC_CREATED;

@Service
public class UserFederationServiceImpl implements UserFederationService {
    @Autowired
    private Keycloak keycloak;

    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String defaultCompany;

    @Value("${ids.keycloak.federation.defaultValue:_UNKNOWN}")
    private String defaultValue;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JobApi jobApi;

    @Value("${service-invocation.root-url:_UNKNOWN}")
    private String serviceRootUrl;

    @Autowired
    private LocalLDAPAuthenticationInfoDao localLDAPAuthenticationInfoDao;

    private static Logger logger = LoggerFactory.getLogger(UserFederationServiceImpl.class);


    Pattern p = Pattern.compile("^[a-zA-Z0-9@._-]{1,50}$");

    //查看所有的认证源信息

    @Override
    public List<ComponentRepresentation> getAuthenticationSources() {
        logger.info("Before getAuthenticationSources");
        List<ComponentRepresentation> componentRepresentations = keycloak.realm(defaultCompany).components().query(null, "org.keycloak.storage.UserStorageProvider");
        Iterator<ComponentRepresentation> iterator = componentRepresentations.iterator();
        while (iterator.hasNext()) {
            if ("IDS".equals(iterator.next().getName())) {
                iterator.remove();
            }
        }
        logger.info("After getAuthenticationSources");
        return componentRepresentations;
    }

    //查看特定的认证源信息

    @Override
    public ComponentRepresentation getAuthenticationSourceInfo(String id) {
        logger.info("Before getAuthenticationSourceInfo");
        ComponentRepresentation componentRepresentation = keycloak.realm(defaultCompany).components().component(id).toRepresentation();
        Map map = getJobMap(componentRepresentation.getName());
        String cronExpression = map == null ? null : map.get("cronExpression").toString();
        componentRepresentation.getConfig().add("syncPeriod", cronExpression);
        return componentRepresentation;
    }

    //删除特定认证源

    @Override
    public void delete(String id) {
        //注意，kc在删除掉某个认证源的时候会自动将同步到该认证源的用户删除掉
        removeImportedUsers(id);
        logger.info("Before delete");
        ComponentResource componentResource = keycloak.realm(defaultCompany).components().component(id);
        String authSourceName = componentResource.toRepresentation().getName();
        componentResource.remove();
        localLDAPAuthenticationInfoDao.delete(id);
        deleteSyncJob(authSourceName);
        logger.info("After delete");

    }


    //测试ldap连接

    @Override
    public Response testConnection(String url) {
        logger.info("Before testConnection");
        Response response = null;
        response = keycloak.realm(defaultCompany).testLDAPConnection("testConnection", url, null, null, "ldapsOnly", "3");
        response.close();
        logger.info("After testConnection");
        return response;
    }

    //测试ldap验证

    @Override
    public Response testAuthentication(String id, String url, String bindDn, String bindCredential) {
        logger.info("Before testAuthentication");
        //为了解决获取LDAP的bindCredential时是一堆*号，导致用户点击测试连接时总是失败，将bindCredential进行后台静默的替换操作
        //如果等于这一堆*，证明是KC返回的秘文密码，则进行替换操作
        if (bindCredential.equals("**********")) {
            //id不为空，证明是获取完认证源信息后触发的
            if (id != null) {
                try {
                    LocalLDAPAuthenticationInfo localLDAPAuthenticationInfo = localLDAPAuthenticationInfoDao.findById(id);
                    if (localLDAPAuthenticationInfo != null) {
                        bindCredential = new String(Base64.getDecoder().decode(localLDAPAuthenticationInfo.getPassword()), "utf-8");
                    }
                } catch (Exception e) {
                    logger.error("在测试验证过程中查找本地密码时出错", e);
                }
            }
        } else {
            if (id != null) {
                //更新本地保存的LDAP密码
                saveLDAPPasswordById(id, bindCredential);
            }
        }
        Response response = keycloak.realm(defaultCompany).testLDAPConnection("testAuthentication", url, bindDn, bindCredential, "ldapsOnly", null);
        response.close();
        logger.info("After testAuthentication");
        return response;
    }

    //新增认证源

    @Override
    public ResponseObject create(AuthenticationSource authenticationSource) {
        ComponentRepresentation newComponentRepresentation = generateComponentRepresentation(null, authenticationSource);
        newComponentRepresentation.setParentId(defaultCompany);
        logger.info("Before Create AuthenticationSource");
        Response response = keycloak.realm(defaultCompany).components().add(newComponentRepresentation);
        if (response.getStatus() == SC_CREATED) {
            Map<String, Object> map = new HashMap<>();
            String[] pathComponents = response.getLocation().getPath().split("/");
            Integer sizeOfPathComponents = pathComponents.length;
            String returnedId = pathComponents[sizeOfPathComponents - 1];
            map.put("id", returnedId);
            authenticationSource.setId(returnedId);
            saveLDAPPassword(authenticationSource);
            logger.info("After Create AuthenticationSource");
            return ResponseObject.newSuccessResponseObject(new JSONObject(map), "Success.");
        } else {
            return ResponseObject.newErrorResponseObject(response.getStatus(), response.toString());
        }
    }

    //更新认证源

    @Override
    public void update(AuthenticationSource authenticationSource) {
        String id = authenticationSource.getId();
        ComponentRepresentation newComponentRepresentation = generateComponentRepresentation(id, authenticationSource);
        logger.info("Before Update AuthenticationSource");
        logger.info("更新认证源时的密码：" + authenticationSource.getBindCredential());
        if (authenticationSource.getBindCredential() != null && !"".equals(authenticationSource.getBindCredential())) {
            logger.info("执行更新认证源密码");
            newComponentRepresentation.getConfig().putSingle("bindCredential", authenticationSource.getBindCredential());
            saveLDAPPassword(authenticationSource);
        } else {
            logger.info("使用默认的本地存储密码");
            String localPassword = localLDAPAuthenticationInfoDao.findById(authenticationSource.getId()).getPassword();
            String decodedPassword = new String(Base64.getDecoder().decode(localPassword.getBytes()));
            newComponentRepresentation.getConfig().putSingle("bindCredential", decodedPassword);
        }
        keycloak.realm(defaultCompany).components().component(id).update(newComponentRepresentation);
        logger.info("After Update AuthenticationSource");
    }


    //认证源名称唯一性校验接口

    @Override
    public ResponseObject uniqueValidation(String id, String authenticationSourceName) {
        //首先从kc获取所有已存在认证源信息
        logger.info("Before uniqueValidation");
        List<ComponentRepresentation> existingAuthenticationSources = getAuthenticationSources();
        if (id == null) {
            for (ComponentRepresentation existingAuthenticationSource : existingAuthenticationSources) {
                if (existingAuthenticationSource.getName().equals(authenticationSourceName)) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.AUTHENTICATION_SOURCE_NAME_CONFLICT_ERROR, ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_CONFLICT_ERROR);
                }
            }
        } else {
            for (ComponentRepresentation existingAuthenticationSource : existingAuthenticationSources) {
                if (existingAuthenticationSource.getName().equals(authenticationSourceName) && !existingAuthenticationSource.getId().equals(id)) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.AUTHENTICATION_SOURCE_NAME_CONFLICT_ERROR, ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_CONFLICT_ERROR);
                }
            }
        }
        logger.info("After uniqueValidation");
        return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
    }


    @Override
    public ResponseObject addOrUpdateSyncJob(String id, String authenticationSourceName, String syncPeriod) {
        Map map = getJobMap(authenticationSourceName);
        String jobId = map == null ? null : map.get("jobId").toString();
        if (jobId != null) {
            JobBean4Update jobBean4Update = new JobBean4Update();
            jobBean4Update.setJobCron(syncPeriod);
            jobBean4Update.setJobId(jobId);
            try {
                jobApi.update(jobBean4Update);
            } catch (Exception e) {
                return ResponseObject.newErrorResponseObject(-1, e.getMessage());
            }
        } else {
            JobBean4Add jobBean4Add = new JobBean4Add();
            jobBean4Add.setJobName(authenticationSourceName + "-synchronize-job");
            jobBean4Add.setJobMethod("POST");
            jobBean4Add.setJobCron(syncPeriod);
            jobBean4Add.setJobGroup("ids-federation-service");
            jobBean4Add.setJobTimeoutSeconds(300);
            jobBean4Add.setJobUrl(serviceRootUrl + "/authenticationSources/" + id + "/triggerFullSync/jobSchedule");
            try {
                jobApi.add(jobBean4Add);
            } catch (Exception e) {
                return ResponseObject.newErrorResponseObject(-1, e.getMessage());
            }
        }
        return ResponseObject.newSuccessResponseObject(0, "Success.");
    }


    private void deleteSyncJob(String authSourceName) {
        Map map = getJobMap(authSourceName);
        String jobId = map == null ? null : map.get("jobId").toString();
        if (jobId != null) {
            jobApi.delete(jobId);
        }
    }

    private Map getJobMap(String authSourceName) {
        ResponseObject responseObject = jobApi.jobs(1, 1, authSourceName + "-synchronize-job", "ids-federation-service");
        if (responseObject.getStatus() == 0 && responseObject.getData() != null) {
            List list = new ArrayList((Collection) ((LinkedHashMap) responseObject.getData()).get("rows"));
            if (list.size() != 0) {
                Map map = (LinkedHashMap) list.get(0);
                return map;
            }
        }
        return null;
    }

    private ComponentRepresentation generateComponentRepresentation(String id, AuthenticationSource authenticationSource) {

        ComponentRepresentation newComponentRepresentation;
        if (id != null) {
            logger.info("Before Generate ComponentRepresentation");
            newComponentRepresentation = keycloak.realm(defaultCompany).components().component(id).toRepresentation();
            logger.info("After Generate ComponentRepresentation");
        } else {
            newComponentRepresentation = new ComponentRepresentation();
        }
        //设置新的UserStorage的基本属性
        newComponentRepresentation.setName(authenticationSource.getAuthenticationSourceName());
        newComponentRepresentation.setProviderId("ldap");
        newComponentRepresentation.setProviderType("org.keycloak.storage.UserStorageProvider");
        //设置新的UserStorage的config属性
        MultivaluedHashMap newConfig;
        newConfig = newComponentRepresentation.getConfig();
        if (newConfig == null) {
            newConfig = new MultivaluedHashMap();
        }

        //默认属性的配置

        if (defaultValue.equals("_UNKNOWN")) {
            defaultValue = ConstantsLibrary.Message.FEDERATION_DEFAULT_VALUE;
        }

        HashMap<String, Object> result;

        try {
            result = new ObjectMapper().readValue(defaultValue, HashMap.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        newConfig.putAll(result);

        //lastSync无须设置

        //用户上传的属性的配置

        setComponentRepresentationConfig("usersDn", authenticationSource.getBaseDN(), newConfig);

        setComponentRepresentationConfig("bindDn", authenticationSource.getBindDN(), newConfig);

        setComponentRepresentationConfig("usernameLDAPAttribute", authenticationSource.getUsernameLDAPAttribute(), newConfig);

        setComponentRepresentationConfig("bindCredential", authenticationSource.getBindCredential(), newConfig);

        if (authenticationSource.getAuthenticationSourceType().equals("LDAP")) {
            setComponentRepresentationConfig("vendor", "other", newConfig);
        } else if (authenticationSource.getAuthenticationSourceType().equals("Active Directory")) {
            setComponentRepresentationConfig("vendor", "ad", newConfig);
        }

        setComponentRepresentationConfig("uuidLDAPAttribute", authenticationSource.getuUIDLDAPAttribute(), newConfig);

        setComponentRepresentationConfig("connectionUrl", authenticationSource.getConnectionURL(), newConfig);

        if (authenticationSource.getAuthenticationType().equals("needPassword")) {
            setComponentRepresentationConfig("authType", "simple", newConfig);
        } else if (authenticationSource.getAuthenticationType().equals("none")) {
            setComponentRepresentationConfig("authType", "none", newConfig);
        }

        setComponentRepresentationConfig("userObjectClasses", authenticationSource.getUserObjectClasses(), newConfig);

        setComponentRepresentationConfig("rdnLDAPAttribute", authenticationSource.getrDNLDAPAttribute(), newConfig);

        setComponentRepresentationConfig("customUserSearchFilter", authenticationSource.getUserLDAPFilter(), newConfig);

        //newConfig配置完毕，把newConfig添加到的新的认证源里
        newComponentRepresentation.setConfig(newConfig);
        return newComponentRepresentation;
    }

    private void setComponentRepresentationConfig(String key, String value, MultivaluedHashMap config) {
        List newValueList = new ArrayList();
        newValueList.add(value);
        config.put(key, newValueList);
    }


    //同步用户相关功能

    //触发用户的同步
    @Override
    public ResponseObject syncUsers(String id, String action) {
        try {
            //首先根据id获取该认证源的名称
            ComponentRepresentation sourceUserStorage = keycloak.realm(defaultCompany).components().component(id).toRepresentation();
            String authenticationSourceName = sourceUserStorage.getName();
            logger.info("开始Keycloak同步用户过程");
            SynchronizationResultRepresentation synchronizationResultRepresentation = keycloak.realm(defaultCompany).userStorage().syncUsers(id, action);
            logger.info("结束Keycloak同步用户过程");
            logger.info(synchronizationResultRepresentation.getStatus());
            //同步成功，开始IDS向KC的用户同步流程
            //首先获取KC新同步的用户
            List<UserRepresentation> userRepresentations = null;
            List<UserRepresentation> syncedUserRepresentations = new ArrayList<>(10240);
            int size = 500;
            int total = keycloak.realm(defaultCompany).users().count();
            int times = (int) Math.ceil((double) total / (double) size);
            int failCount = 0;
            int successCount = 0;
            for (int i = 0; i < times; i++) {
                userRepresentations = keycloak.realm(defaultCompany).users().list(size * i, size);
                logger.info("第" + (i + 1) + "次循环，获取用户数：" + userRepresentations.size());
                for (UserRepresentation user : userRepresentations) {
                    if (user.getFederationLink() != null && user.getFederationLink().equals(id)) {
                        syncedUserRepresentations.add(user);
                    }
                }
                logger.info("第" + (i + 1) + "次循环，缓冲表用户数：" + syncedUserRepresentations.size());
            }

            for (UserRepresentation syncedUser : syncedUserRepresentations) {
                User idsUser = new User();
                idsUser.setId(syncedUser.getId());
                idsUser.setUsername(syncedUser.getUsername());
                idsUser.setEmail(syncedUser.getEmail());
                idsUser.setFirstName(syncedUser.getLastName() + syncedUser.getFirstName());
                idsUser.setEnabled(syncedUser.isEnabled());
                idsUser.setCompany(defaultCompany);
                idsUser.setFederationLink(syncedUser.getFederationLink());
                idsUser.setFederationName(authenticationSourceName);
                logger.info("开始同步用户：" + idsUser.getUsername());
                try {
                    userDao.saveAndFlush(idsUser);
                    successCount++;
                } catch (Exception e) {
                    logger.error("同步异常:" + idsUser.getUsername() + "同步失败。", e);
                    failCount++;
                    continue;
                }
            }
            String SyncResultLog = "同步成功,失败用户:" + (failCount + synchronizationResultRepresentation.getFailed()) + ";成功用户:" + successCount;
            logger.info(SyncResultLog);
            return ResponseObject.newSuccessResponseObject(SyncResultLog, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("同步异常", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }

    }

    private void saveLDAPPasswordById(String id, String password) {
        LocalLDAPAuthenticationInfo localLDAPAuthenticationInfo = new LocalLDAPAuthenticationInfo();
        localLDAPAuthenticationInfo.setId(id);
        String savedPassword = password;
        //应该进行加密操作
        String encryptedPassword = savedPassword;
        try {
            encryptedPassword = Base64.getEncoder().encodeToString(savedPassword.getBytes("utf-8"));
        } catch (Exception e) {
            logger.error("对ldap密码进行base64编码时出错", e);
        }

        localLDAPAuthenticationInfo.setPassword(encryptedPassword);
        try {
            localLDAPAuthenticationInfoDao.save(localLDAPAuthenticationInfo);
        } catch (Exception e) {
            logger.error("保存LDAP密码出错", e);
        }
    }

    private void saveLDAPPassword(AuthenticationSource authenticationSource) {
        String id = authenticationSource.getId();
        LocalLDAPAuthenticationInfo localLDAPAuthenticationInfo = new LocalLDAPAuthenticationInfo();
        localLDAPAuthenticationInfo.setId(id);
        String savedPassword = authenticationSource.getBindCredential();
        //应该进行加密操作
        String encryptedPassword = savedPassword;
        try {
            encryptedPassword = Base64.getEncoder().encodeToString(savedPassword.getBytes("utf-8"));
        } catch (Exception e) {
            logger.error("对ldap密码进行base64编码时出错", e);
        }

        localLDAPAuthenticationInfo.setPassword(encryptedPassword);
        try {
            localLDAPAuthenticationInfoDao.save(localLDAPAuthenticationInfo);
        } catch (Exception e) {
            logger.error("保存LDAP密码出错", e);
        }

    }

    //移除IDS用户的同步
    @Override
    public ResponseObject removeImportedUsers(String id) {
        try {
            //移除IDS本地的用户
            List<User> usersNeedRemove = userDao.findByFederationLink(id);
            for (User userNeedRemove : usersNeedRemove) {
                userDao.delete(userNeedRemove.getId());
            }
            //然后移除KC同步过的用户
            keycloak.realm(defaultCompany).userStorage().removeImportedUsers(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("removeImportedUsers Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    //周期性的完全同步所触发的方法

    @Override
    public ResponseObject scheduledSyncUsers(String id) {
        try {
            removeImportedUsers(id);
            return syncUsers(id, "triggerFullSync");
        } catch (Exception e) {
            logger.error("定时同步用户失败", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    //测试接口，用来测试微服务连通性
    @Override
    public ResponseObject testAvailability() {
        String resultString = "微服务可用。";
        return ResponseObject.newSuccessResponseObject(resultString, ConstantsLibrary.Message.SUCCESS);
    }
}

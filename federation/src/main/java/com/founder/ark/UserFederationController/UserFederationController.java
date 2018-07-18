package com.founder.ark.UserFederationController;


import com.founder.ark.Dao.LocalLDAPAuthenticationInfoDao;
import com.founder.ark.ModuleClass.AuthSourceNameUniqueValidationParams;
import com.founder.ark.ModuleClass.AuthenticationSource;
import com.founder.ark.UserFederationInterface.UserFederationService;
import com.founder.ark.Utilities.ConstantsLibrary;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.core.util.NotCheckToken;
import com.founder.ark.support.task.api.JobApi;

import org.keycloak.representations.idm.ComponentRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.MediaType;


import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.apache.http.HttpStatus.*;

@RestController
@EnableFeignClients
public class UserFederationController {

    @Autowired
    private UserFederationService userFederationService;

    @Value("${ids.keycloak.federation.inherent:_UNKNOWN}")
    private String inherentFederation;

    //动态定时任务的依赖注入类(从网上copy)
    private static final String JOB_GROUP = "event_job_group";
    private static final String TRIGGER_GROUP = "event_trigger_group";

    @Autowired
    private LocalLDAPAuthenticationInfoDao localLDAPAuthenticationInfoDao;


    //记录日志的类
    private static Logger logger = LoggerFactory.getLogger(UserFederationController.class);

    //用于校验上传的AuthenticationSource的参数信息
    private ResponseObject validateAuthenticationSource(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            List<ObjectError> sortedErrors = new ArrayList<ObjectError>(errors);

            if (sortedErrors.size() > 0) {
                sortedErrors.sort(new Comparator<ObjectError>() {
                    @Override
                    public int compare(ObjectError o1, ObjectError o2) {
                        return o1.getDefaultMessage().compareTo(o2.getDefaultMessage());
                    }
                });
            }
            for (ObjectError error : sortedErrors) {
                if (ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_NULL_ERROR.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.AUTHENTICATION_SOURCE_NAME_NULL_ERROR, ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_NULL_ERROR);
                }
                if (ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_TOO_LONG_ERROR.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.AUTHENTICATION_SOURCE_NAME_TOO_LONG_ERROR, ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_TOO_LONG_ERROR);
                }
                if (ConstantsLibrary.Message.USER_LDAP_FILTER_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USER_LDAP_FILTER_ILLEGAL, ConstantsLibrary.Message.USER_LDAP_FILTER_ILLEGAL);
                }
            }
        }

        return null;
    }

    //获取所有认证源信息
    @RequestMapping("/authenticationSources")
    public ResponseObject getUserStorageComponents() {
        List<ComponentRepresentation> userStorageComponents = userFederationService.getAuthenticationSources();
        return ResponseObject.newSuccessResponseObject(userStorageComponents, ConstantsLibrary.Message.SUCCESS);
    }

    //获取特定认证源信息
    @GetMapping("/authenticationSources/{id}")
    public ResponseObject getAuthenticationSourceInfo(@PathVariable String id) {
        ComponentRepresentation componentRepresentation = userFederationService.getAuthenticationSourceInfo(id);
        return ResponseObject.newSuccessResponseObject(componentRepresentation, ConstantsLibrary.Message.SUCCESS);
    }

    //新增定时同步的任务

    @PostMapping("/authenticationSources/{id}/syncJob")
    public ResponseObject createSyncJob(@PathVariable String id, @RequestBody SyncJobTmp syncJobTmp) {
        logger.info("新建同步任务：" + syncJobTmp.getAuthenticationSourceName() + "," + syncJobTmp.getSyncPeriod());
        return userFederationService.addOrUpdateSyncJob(id, syncJobTmp.getAuthenticationSourceName(), syncJobTmp.getSyncPeriod());
    }

    //更新定时同步的任务

    @PutMapping("/authenticationSources/{id}/syncJob")
    public ResponseObject updateSyncJob(@PathVariable String id, @RequestBody SyncJobTmp syncJobTmp) {
        logger.info("更新同步任务：" + syncJobTmp.getAuthenticationSourceName() + "," + syncJobTmp.getSyncPeriod());
        return userFederationService.addOrUpdateSyncJob(id, syncJobTmp.getAuthenticationSourceName(), syncJobTmp.getSyncPeriod());
    }


    //测试LDAP连接
    @PostMapping("/authenticationSources/testConnection")
    public ResponseObject testConnection(@RequestBody TestLDAPConnectionParams testLDAPConnectionParams) {
        Response response = userFederationService.testConnection(testLDAPConnectionParams.getConnectionUrl());
        if (response.getStatus() == SC_NO_CONTENT) {
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } else {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.TEST_CONNECTION_ERROR, ConstantsLibrary.Message.TEST_CONNECTION_ERROR);
        }
    }

    //测试LDAP验证
    @PostMapping("/authenticationSources/testAuthentication")
    public ResponseObject testAuthentication(@RequestBody TestLDAPConnectionParams testLDAPConnectionParams) {
        Response response = userFederationService.testAuthentication(testLDAPConnectionParams.getId(), testLDAPConnectionParams.getConnectionUrl(), testLDAPConnectionParams.getBindDn(), testLDAPConnectionParams.getBindCredential());
        if (response.getStatus() == SC_NO_CONTENT) {
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } else {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.TEST_AUTHENTICATION_ERROR, ConstantsLibrary.Message.TEST_AUTHENTICATION_ERROR);
        }
    }

    //新增认证源
    @PostMapping("/authenticationSources")
    public ResponseObject create(@RequestBody @Valid AuthenticationSource authenticationSource, BindingResult bindingResult) {
        //首先进行上传参数校验
        ResponseObject responseObject = validateAuthenticationSource(bindingResult);
        if (responseObject != null) {
            return responseObject;
        }
        //进行认证源名称唯一性校验
        ResponseObject responseObject1 = userFederationService.uniqueValidation(null, authenticationSource.getAuthenticationSourceName());
        if (responseObject1.getStatus() != 0) {
            return responseObject1;
        }
        return userFederationService.create(authenticationSource);
    }


    //删除认证源
    @DeleteMapping("/authenticationSources/{id}")
    public ResponseObject deleteAuthSource(@PathVariable String id) {
        //获取需要屏蔽的LDAP源，防止开发人员误删
        List<String> inherentFederationIds = new ArrayList<>();
        if (inherentFederation.equals("_UNKNOWN")) {
            logger.info("找不到任何已存在的认证源过滤条件，删除取消。");
            return ResponseObject.newSuccessResponseObject("找不到任何已存在的认证源过滤条件，删除取消。", ConstantsLibrary.Message.SUCCESS);
        }
        try {
            inherentFederationIds = Arrays.asList(inherentFederation.split(","));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ComponentRepresentation willDeletedAuthenticationSource = new ComponentRepresentation();
        try {
            willDeletedAuthenticationSource = userFederationService.getAuthenticationSourceInfo(id);
        } catch (Exception e) {
            logger.error("delete authenticationSources Exception in validate whether the will deleted authenticationSource is consist of inherent ldap", e);
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.AUTHENTICATION_SOURCE_DELETE_ERROR, ConstantsLibrary.Message.AUTHENTICATION_SOURCE_DELETE_ERROR);
        }


        if (inherentFederationIds.contains(willDeletedAuthenticationSource.getName())) {
            return ResponseObject.newSuccessResponseObject("该认证源是系统固有的，您无法删除，抱歉！", ConstantsLibrary.Message.SUCCESS);
        }
        //过滤完成，开始实际的删除进程
        try {
            userFederationService.delete(id);
            //在成功返回之间记录一个日志
            logger.info("删除认证源");
            //在删除一个认证源后从本地删除该认证源的认证密码
            deleteLDAPPassword(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("delete authenticationSources Exception", e);
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.AUTHENTICATION_SOURCE_DELETE_ERROR, ConstantsLibrary.Message.AUTHENTICATION_SOURCE_DELETE_ERROR);
        }
    }


    //更新认证源
    @PutMapping(value = "/authenticationSources/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject update(@PathVariable String id, @RequestBody @Valid AuthenticationSource authenticationSource, BindingResult bindingResult) {
        //首先进行上传参数校验
        ResponseObject responseObject = validateAuthenticationSource(bindingResult);
        if (responseObject != null) {
            return responseObject;
        }
        //进行认证源名称唯一性校验,更新的时候认证源id不为null
        ResponseObject responseObject1 = userFederationService.uniqueValidation(id, authenticationSource.getAuthenticationSourceName());
        if (responseObject1.getStatus() != 0) {
            return responseObject1;
        }
        authenticationSource.setId(id);
        try {
            userFederationService.update(authenticationSource);
            //在成功返回之间记录一个日志
            logger.info("更新认证源");
            //然后更新认证源
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("update authenticationSources Exception", e);
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }
    }

    //认证源名称唯一性校验
    @PostMapping("/authenticationSources/uniqueValidation/authenticationSourceName")
    public ResponseObject uniqueValidation(@RequestBody AuthSourceNameUniqueValidationParams authSourceNameUniqueValidationParams) {
        ResponseObject responseObject = userFederationService.uniqueValidation(authSourceNameUniqueValidationParams.getId(), authSourceNameUniqueValidationParams.getAuthenticationSourceName());
        return responseObject;
    }

    //封装了测试连接时候的body类
    private static class TestLDAPConnectionParams {
        String connectionUrl;

        String bindDn;

        String bindCredential;

        //认证源的id，当新增认证源时，不需要传该id；当编辑认证源时，需要传该id
        String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBindDn() {
            return bindDn;
        }

        public void setBindDn(String bindDn) {
            this.bindDn = bindDn;
        }

        public String getBindCredential() {
            return bindCredential;
        }

        public void setBindCredential(String bindCredential) {
            this.bindCredential = bindCredential;
        }

        public String getConnectionUrl() {
            return connectionUrl;
        }

        public void setConnectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
        }
    }

    //同步用户相关功能

    //同步指定LDAP的全部用户
    @PostMapping("/authenticationSources/{id}/triggerFullSync")
    public ResponseObject triggerFullSyncOfAuthenticationSource(@PathVariable String id) {
        return triggerFullSync(id);
    }

    //同步指定LDAP的全部用户
    @PostMapping("/authenticationSources/{id}/triggerFullSync/jobSchedule")
    @NotCheckToken
    public ResponseObject triggerFullSync4JobSchedule(@PathVariable String id) {
        logger.info("任务调度程序触发同步开始。");
        return triggerFullSync(id);
    }

    private ResponseObject triggerFullSync(@PathVariable String id) {
        //获取需要屏蔽的LDAP源，防止开发人员误同步
        List<String> inherentFederationIds;
        if (inherentFederation.equals("_UNKNOWN")) {
            logger.info("找不到任何已存在的认证源过滤条件，同步取消。");
            return ResponseObject.newSuccessResponseObject("找不到任何已存在的认证源过滤条件，同步取消。", ConstantsLibrary.Message.SUCCESS);
        }
        try {
            inherentFederationIds = Arrays.asList(inherentFederation.split(","));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ComponentRepresentation willDeletedAuthenticationSource = userFederationService.getAuthenticationSourceInfo(id);
        if (inherentFederationIds.contains(willDeletedAuthenticationSource.getName())) {
            return ResponseObject.newSuccessResponseObject("该认证源是系统固有的，您无法同步，抱歉！", ConstantsLibrary.Message.SUCCESS);
        }
        //过滤完成，开始实际的同步进程
        return userFederationService.scheduledSyncUsers(id);
    }

    //移除指定LDAP的全部用户
    @PostMapping("/authenticationSources/{id}/removeImportedUsers")
    public ResponseObject removeImportedUsersOfAuthenticationSource(@PathVariable String id) {
        return userFederationService.removeImportedUsers(id);
    }

    //以下是测试用接口
    @GetMapping("/authenticationSources/scheduledSyncUsers/{id}")
    public ResponseObject scheduledSyncUsers(@PathVariable String id) {
        return userFederationService.scheduledSyncUsers(id);
    }

    //测试接口，用来测试微服务连通性
    @GetMapping("/authenticationSources/testAvailability")
    public ResponseObject testAvailability() {
        //dynamicScheduledTask.setCron("0/10 * * * * ?");

        //copied from internet


        return userFederationService.testAvailability();
    }


    private static class SyncJobTmp {

        String authenticationSourceName;
        String syncPeriod;

        public String getAuthenticationSourceName() {
            return authenticationSourceName;
        }

        public String getSyncPeriod() {
            return syncPeriod;
        }
    }

    //删除一个LDAP的id和密码
    private void deleteLDAPPassword(String id) {
        try {
            localLDAPAuthenticationInfoDao.delete(id);
            logger.info("删除了id:" + id + "的本地密码");
        } catch (Exception e) {
            logger.error("删除LDAP密码出错", e);
        }

    }

}

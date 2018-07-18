package com.founder.ark.UserFederationInterface;

import com.founder.ark.ModuleClass.AuthenticationSource;
import com.founder.ark.common.utils.bean.ResponseObject;
import org.keycloak.representations.idm.ComponentRepresentation;

import javax.ws.rs.core.Response;
import java.util.List;

public interface UserFederationService {
    //获取所有的认证源信息
    List<ComponentRepresentation> getAuthenticationSources();

    //查看特定认证源信息
    ComponentRepresentation getAuthenticationSourceInfo(String id);

    //新增认证源接口
    ResponseObject create(AuthenticationSource authenticationSource);

    //删除特定认证源
    void delete(String id);

    //更新认证源
    void update(AuthenticationSource authenticationSource);

    //LDAP-测试连接接口
    Response testConnection(String url);

    //LDAP-测试验证接口
    Response testAuthentication(String id, String url, String bindDn, String bindCredential);

    //认证源名称唯一性校验接口
    ResponseObject uniqueValidation(String authId,String authSourceName);

    //用户同步功能相关接口

    //触发kc上用户的同步
    ResponseObject syncUsers(String id, String action);

    //移除IDS上从特定LDAP同步过来的用户
    ResponseObject removeImportedUsers(String id);

    //周期性的完全同步所触发的方法
    ResponseObject scheduledSyncUsers(String id);

    //测试接口，用来测试微服务的连通性
    ResponseObject testAvailability();

    //设置定时同步任务
    ResponseObject addOrUpdateSyncJob(String id , String authenticationSourceName,String syncPeriod);

}

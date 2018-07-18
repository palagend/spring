package com.founder.ark.ids.service.core.bean.keycloak;

import com.founder.ark.ids.service.core.bean.ConstantsLibrary;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * Created by cheng.ly on 2018/2/27.
 */
@Entity
public class Client {
    /**Client的UUID*/
    @Id
    String id;
    /**Client的ClientID*/
    @NotNull(message = ConstantsLibrary.Message.CLIENT_NAME_NOT_NULL)
    @Length(max = 50, message = ConstantsLibrary.Message.CLIENT_NAME_ILLEGAL)
    String appName;

    @NotNull(message = ConstantsLibrary.Message.ICON_URL_NOT_NULL)
    String iconUrl;

    long createdTimestamp;

    @NotNull(message = ConstantsLibrary.Message.LOGIN_URL_NOT_NULL)
    @Pattern(regexp = "(http|https):\\/\\/([\\w.]+\\/?)\\S*",message = ConstantsLibrary.Message.LOGIN_URL_ILLEGAL)
    String loginUrl;

    String baseApp;

    String identifier;

    @Transient
    List<Group> assignedGroups;

    public List<Group> getAssignedGroups() {
        return assignedGroups;
    }

    public void setAssignedGroups(List<Group> assignedGroups) {
        this.assignedGroups = assignedGroups;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getBaseApp() {
        return baseApp;
    }

    public void setBaseApp(String baseApp) {
        this.baseApp = baseApp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Client() {
    }

    public Client(String id, String appName) {
        this.id = id;
        this.appName = appName;
    }

    public Client(String id, String appName, String iconUrl, String loginUrl) {
        this.id = id;
        this.appName = appName;
        this.iconUrl = iconUrl;
        this.loginUrl = loginUrl;
    }
}

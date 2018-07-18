package com.founder.ark.ModuleClass;

import com.founder.ark.Utilities.ConstantsLibrary;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

public class AuthenticationSource {

    String id;

    @NotEmpty(message = ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_NULL_ERROR)
    @Pattern(regexp = "^[\\s\\S]{1,50}$", message = ConstantsLibrary.Message.AUTHENTICATION_SOURCE_NAME_TOO_LONG_ERROR)
    String authenticationSourceName;

    String authenticationSourceType;

    String usernameLDAPAttribute;

    String rDNLDAPAttribute;

    String uUIDLDAPAttribute;

    String userObjectClasses;

    String connectionURL;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String baseDN;

    @Pattern(regexp = "(^$)|(^\\([\\s\\S]*\\)$)", message = ConstantsLibrary.Message.USER_LDAP_FILTER_ILLEGAL)
    String userLDAPFilter;

    String authenticationType;

    String bindDN;

    String bindCredential;

    String syncPeriod;

    public String getSyncPeriod() {
        return syncPeriod;
    }

    public void setSyncPeriod(String syncPeriod) {
        this.syncPeriod = syncPeriod;
    }

    public String getAuthenticationSourceName() {
        return authenticationSourceName;
    }

    public void setAuthenticationSourceName(String authenticationSourceName) {
        this.authenticationSourceName = authenticationSourceName;
    }

    public String getAuthenticationSourceType() {
        return authenticationSourceType;
    }

    public void setAuthenticationSourceType(String authenticationSourceType) {
        this.authenticationSourceType = authenticationSourceType;
    }

    public String getUsernameLDAPAttribute() {
        return usernameLDAPAttribute;
    }

    public void setUsernameLDAPAttribute(String usernameLDAPAttribute) {
        this.usernameLDAPAttribute = usernameLDAPAttribute;
    }

    public String getUserObjectClasses() {
        return userObjectClasses;
    }

    public void setUserObjectClasses(String userObjectClasses) {
        this.userObjectClasses = userObjectClasses;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getUserLDAPFilter() {
        return userLDAPFilter;
    }

    public void setUserLDAPFilter(String userLDAPFilter) {
        this.userLDAPFilter = userLDAPFilter;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getBindDN() {
        return bindDN;
    }

    public void setBindDN(String bindDN) {
        this.bindDN = bindDN;
    }

    public String getBindCredential() {
        return bindCredential;
    }

    public void setBindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
    }

    public String getrDNLDAPAttribute() {
        return rDNLDAPAttribute;
    }

    public void setrDNLDAPAttribute(String rDNLDAPAttribute) {
        this.rDNLDAPAttribute = rDNLDAPAttribute;
    }

    public String getuUIDLDAPAttribute() {
        return uUIDLDAPAttribute;
    }

    public void setuUIDLDAPAttribute(String uUIDLDAPAttribute) {
        this.uUIDLDAPAttribute = uUIDLDAPAttribute;
    }
}



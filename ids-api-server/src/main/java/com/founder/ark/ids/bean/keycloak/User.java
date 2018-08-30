package com.founder.ark.ids.bean.keycloak;

import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.util.StringHelper;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.keycloak.representations.idm.UserRepresentation;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @author huyh (mailto:huyh@founder.com).
 */
@Entity
@ToString
public class User {
    @NotEmpty(message = ConstantsLibrary.Message.USERNAME_NULL)
    @Pattern(regexp = "^[a-zA-Z0-9@._-]{1,50}$", message = ConstantsLibrary.Message.USERNAME_ILLEGAL)
    String username;
    @Pattern(regexp = "(^((\\+86)|(86))?1[34578]\\d{9}$)|(^$)", message = ConstantsLibrary.Message.MOBILE_ILLEGAL)
    String mobilePhone;
    String company;
    @Email(message = ConstantsLibrary.Message.EMAIL_ILLEGAL, regexp = StringHelper.EMAIL_REGEX)
    @NotEmpty(message = ConstantsLibrary.Message.EMAIL_NULL)
    String email;
    @NotEmpty(message = ConstantsLibrary.Message.NAME_EMPTY)
    @Pattern(regexp = "^[-._@a-zA-Z0-9\u4e00-\u9fff]{1,30}$", message = ConstantsLibrary.Message.NAME_ILLEGAL)
    String firstName;
    Boolean enabled;

    public static User wrap(UserRepresentation userRepresentation) {
        User user = new User();
        user.setUsername(userRepresentation.getUsername());

        return user;
    }

    public String getFederationLink() {
        return federationLink;
    }

    public void setFederationLink(String federationLink) {
        this.federationLink = federationLink;
    }

    //用户联合字段
    String federationLink;

    //用户联合名称
    String federationName;
    @Transient
    Boolean temporary;

    public Boolean getTemporary() {
        return temporary;
    }

    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }

    @Transient
    List<String> groups;
    @Transient
    List<String> apps;
    @Transient
    List<String> roles;

    @Id
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getApps() {
        return apps;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFederationName() {
        return federationName;
    }

    public void setFederationName(String federationName) {
        this.federationName = federationName;
    }
}


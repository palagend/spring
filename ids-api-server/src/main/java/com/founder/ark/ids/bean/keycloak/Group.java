package com.founder.ark.ids.bean.keycloak;

import com.founder.ark.ids.bean.ConstantsLibrary;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "ids_group")
public class Group {
    @NotEmpty(message = ConstantsLibrary.Message.GROUP_NAME_EMPTY)
    @Pattern(regexp = "^[-._@a-zA-Z0-9\u4e00-\u9fff]{1,50}$", message = ConstantsLibrary.Message.GROUP_NAME_ILLEGAL)
    String groupName;
    @Pattern(regexp = "^[\\w\\W]{0,50}$", message = ConstantsLibrary.Message.GROUP_DESC_LENGTH)
    String groupDescription;
    @Id
    String id;
    String groupType;

    String[] apps;
    String[] users;

    //创建时间
    long createdTimestamp;
    //更新时间
    long updatedTimestamp;

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(long updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    String company;


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String[] getApps() {
        return apps;
    }

    public void setApps(String[] apps) {
        this.apps = apps;
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }


}

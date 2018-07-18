package com.founder.ark.ModuleClass;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LocalLDAPAuthenticationInfo {

    @Id
    String id;

    String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

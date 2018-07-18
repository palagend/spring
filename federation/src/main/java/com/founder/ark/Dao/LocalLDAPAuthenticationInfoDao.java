package com.founder.ark.Dao;

import com.founder.ark.ModuleClass.LocalLDAPAuthenticationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalLDAPAuthenticationInfoDao  extends JpaRepository<LocalLDAPAuthenticationInfo, String>{
    LocalLDAPAuthenticationInfo findById(String id);
}

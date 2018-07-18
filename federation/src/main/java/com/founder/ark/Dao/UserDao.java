package com.founder.ark.Dao;

import com.founder.ark.ids.service.core.bean.keycloak.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface UserDao extends JpaRepository<User, String> {

    List<User> findByFederationLink(String id);

}

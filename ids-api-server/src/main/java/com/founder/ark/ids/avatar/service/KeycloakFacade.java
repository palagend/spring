package com.founder.ark.ids.avatar.service;

import com.founder.ark.ids.avatar.representations.GroupWrapper;
import com.founder.ark.ids.avatar.representations.UserWrapper;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.support.PagedListHolder;

import javax.ws.rs.core.Response;

/**
 * <p>操作keycloak的facade</p>
 *
 * @author 胡月恒
 * @mail huyh@founder.com
 * @date 2018-08-14
 */
public interface KeycloakFacade {
    Response createOrUpdate(UserWrapper user);

    Response createOrUpdate(GroupWrapper group);

    Response delete(String id);

    UserWrapper findByUsername(String username);

    PagedListHolder<UserRepresentation> list(String searchString);

    UserWrapper detail(String id);

    GroupWrapper groupDetail(String id);

    void update(UserWrapper user);

    void disable(String id);

    void enable(String id);

    void resetPassword(String id);

    boolean isUsernameVacant(String username);

    boolean isEmailVacant(String email);

    PagedListHolder<GroupRepresentation> groups(String groupName);

    void groupDelete(String id);
}

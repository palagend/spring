package com.founder.ark.ids.admin.service.impl;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.admin.dao.UserDao;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.bean.keycloak.User;
import com.founder.ark.ids.service.SessionService;
import com.founder.ark.ids.service.UserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("SessionServiceImpl")
public class SessionServiceImpl implements SessionService {
    @Autowired
    private Keycloak keycloak;

    @Autowired
    UserService userService;
    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String defaultCompany;

    @Autowired
    private UserDao userDao;

    private static Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

    //获取所有在线用户,开发人员：侯逸仙
    @Override
    public PageData<User> getOnlineUsers() {
        Integer pageSize = 9999;
        Integer pageNumber = 1;
        PageData<User> users = userService.users(pageNumber, pageSize, null);
        PageData<User> onlineUsers = new PageData<>();
        List<User> onlineUsersRows = new ArrayList<>();
        for (User user :
                users.getRows()) {
            ///判断某个用户当前是否具有活跃的会话，如果有的话，则把该用户加入到在线用户列表中
            //获取某个用户的当前会话
            List<UserSessionRepresentation> userSessionRepresentations = keycloak.realm(defaultCompany).users().get(user.getId()).getUserSessions();
            //如果会话数量不为0
            if (userSessionRepresentations.size() > 0) {
                onlineUsersRows.add(user);
            }

        }
        onlineUsers.setTotal(onlineUsersRows.size());
        onlineUsers.setPageSize(pageSize);
        onlineUsers.setPageNumber(pageNumber);
        onlineUsers.setRows(onlineUsersRows);
        return onlineUsers;
    }

    //判断指定用户里面有多少用户在线, 开发人员：侯逸仙
    @Override
    public List<User> getOnlineUsersInDesignatedUsers(List<User> designatedUsers) {
        List<User> onlineUsers = new ArrayList<>();
        for (User user :
                designatedUsers) {
            ///判断某个用户当前是否具有活跃的会话，如果有的话，则把该用户加入到搜索到的在线用户列表中
            //获取某个用户的当前会话
            List<UserSessionRepresentation> userSessionRepresentations = keycloak.realm(defaultCompany).users().get(user.getId()).getUserSessions();
            //如果会话数量不为0
            if (userSessionRepresentations.size() > 0) {
                onlineUsers.add(user);
            }

        }
        return onlineUsers;
    }

    //删除单条会话记录，开发人员：侯逸仙
    @Override
    public ResponseObject deleteSession(String id) {
        try {
            keycloak.realm(defaultCompany).deleteSession(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("deleteSession exception", e);
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.SESSION_DELETE_ERROR, ConstantsLibrary.Message.SESSION_DELETE_ERROR);
        }

    }

    //获取一个用户的所有session信息,开发人员：侯逸仙
    @Override
    public List<UserSessionRepresentation> getUserSessions(String id) {
        List<UserSessionRepresentation> userSessions = keycloak.realm(defaultCompany).users().get(id).getUserSessions();
        return userSessions;
    }

    //注销用户，开发人员：侯逸仙
    @Override
    public ResponseObject userLogout(String id) {
        keycloak.realm(defaultCompany).users().get(id).logout();
        Optional<User> user = userDao.findById(id);
        if (user.isPresent()) {
            logger.info(user.get().getUsername() + "注销成功");
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } else
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USER_LOGOUT_ERROR, ConstantsLibrary.Message.USER_LOGOUT_ERROR);
    }

    //批量注销用户，开发人员：侯逸仙
    @Override
    public ResponseObject userBatchLogout(List<String> ids) {
        for (String id : ids) {
            userLogout(id);
        }
        return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
    }
}

package com.founder.ark.ids.admin.controller;


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
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController(value = "kcSessionController")
@RequestMapping("/kc/admin")
@Validated
public class SessionController {
    @Autowired
    SessionService sessionService;
    @Autowired
    UserService userService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private Keycloak keycloak;
    @Value("${ids.keycloak.company:_UNKNOWN}")
    private String company;

    //静态内部类，用于存储获取到的在线用户信息
    final public static class OnlineUsers {
        static public List<User> onlineUsers = null;
        static public boolean shouldRequestNewData = true;
    }

    private static Logger logger = LoggerFactory.getLogger(SessionController.class);

    //定时器，目前设置为2s，设置经过多久需要重新向KC获取一次全部的在线用户
    @Scheduled(fixedDelay = 2000)
    public void doSomething() {
        OnlineUsers.shouldRequestNewData = true;
    }

    //获取所有在线用户，开发人员：侯逸仙
    @RequestMapping(value = "/sessions/onlineUsers", method = RequestMethod.GET)
    public ResponseObject getUsers(Integer pageNumber, Integer pageSize, String searchString) {
        //首先验证pageNumber和pageSize的合法性
        if (pageNumber != null) {
            if (pageNumber < 1) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.Invalid_PageNumber, ConstantsLibrary.Message.Invalid_PageNumber);
            }
        } else {
            pageNumber = 1;//default value
        }
        if (pageSize != null) {
            if (pageSize < 1 || pageSize > 9999) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.Invalid_PageSize, ConstantsLibrary.Message.Invalid_PageSize);
            }
        } else {
            pageSize = 20;//default value
        }
        //统一的返回给前台的分页数据，用于普通查询的分页、缓存的分页以及搜索在线用户的分页
        PageData<User> pagedOnlineUsers = new PageData<>();
        //存储每次请求中临时需要的所有用户数据
        List<User> temporaryUsers = new ArrayList<>();
        if (searchString != null) {
            //用户触发了搜索在线用户功能
            List<User> searchedResultByUsernameOrFirstName = userDao.searchUsersByUsernameOrFirstName(searchString, company);
            List<User> searchedOnlineUsers = sessionService.getOnlineUsersInDesignatedUsers(searchedResultByUsernameOrFirstName);
            temporaryUsers = searchedOnlineUsers;

        } else if (OnlineUsers.shouldRequestNewData == true) {
            try {
                PageData<User> onlineUsers = sessionService.getOnlineUsers();
                //首先将所有的在线用户列表放到公共类中
                OnlineUsers.onlineUsers = onlineUsers.getRows();
                //向KC请求完一次在线用户数据后，将标志位值为false
                OnlineUsers.shouldRequestNewData = false;
                temporaryUsers = OnlineUsers.onlineUsers;
            } catch (Exception e) {
                logger.error("getOnlineUsers exception", e);
                return ResponseObject.newErrorResponseObject(-1, e.getMessage());
            }
        } else {
            temporaryUsers = OnlineUsers.onlineUsers;
        }
        //根据pageNumber和pageSize的值设置PageData中应该返回给前台的的数据
        Integer first = pageSize * ( pageNumber - 1 );
        Integer last = pageNumber * pageSize - 1;

        //设置分页后数据的页码、页数及数据总数
        pagedOnlineUsers.setPageNumber(pageNumber);
        pagedOnlineUsers.setPageSize(pageSize);
        pagedOnlineUsers.setTotal(temporaryUsers.size());
        if (first > temporaryUsers.size() - 1) {
            pagedOnlineUsers.setRows(null);
            return ResponseObject.newSuccessResponseObject(pagedOnlineUsers, ConstantsLibrary.Message.SUCCESS);
        } else if (last > temporaryUsers.size() - 1) {
            pagedOnlineUsers.setRows(temporaryUsers.subList(first, temporaryUsers.size()));
            return ResponseObject.newSuccessResponseObject(pagedOnlineUsers, ConstantsLibrary.Message.SUCCESS);
        } else {
            pagedOnlineUsers.setRows(temporaryUsers.subList(first, last + 1));
            return ResponseObject.newSuccessResponseObject(pagedOnlineUsers, ConstantsLibrary.Message.SUCCESS);
        }

    }

    //删除单挑会话记录，开发人员：侯逸仙
    @RequestMapping(value = "sessions/{sessionId}", method = RequestMethod.DELETE)
    public ResponseObject delete(@PathVariable String sessionId) {
        return sessionService.deleteSession(sessionId);
    }

    //获取一个用户的所有session信息,开发人员：侯逸仙
    @GetMapping(value = "sessions/{userId}/sessions")
    public ResponseObject getUserSessions(@PathVariable String userId) {
        try {
            List<UserSessionRepresentation> userSessionRepresentations =  sessionService.getUserSessions(userId);
            return ResponseObject.newSuccessResponseObject(userSessionRepresentations, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getUserSessions Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    //根据用户id注销某个用户，开发人员：侯逸仙
    @PostMapping(value = "/sessions/onlineUsers/{userId}/logout")
    public ResponseObject userLogout(@PathVariable String userId) {
        //把用户从本地的缓存中删除
        User willDeletedUser = null;
        for (User cachedUser : OnlineUsers.onlineUsers) {
            if (cachedUser.getId().equals(userId)) {
                willDeletedUser = cachedUser;
            }
        }
        OnlineUsers.onlineUsers.remove(willDeletedUser);

        return sessionService.userLogout(userId);
    }

    //根据用户id数组批量注销某些用户，开发人员：侯逸仙
    @PostMapping(value = "/sessions/onlineUsers/actions/delete", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject userBatchLogout(@RequestBody @Valid IDS ids) {
        return sessionService.userBatchLogout(ids.getIds());
    }

    //封装了批量注销时提交的body类
    private static class IDS {
        List<String> ids;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }
    }
}


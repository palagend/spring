package com.founder.ark.ids.admin.controller;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.admin.dao.GroupDao;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.bean.keycloak.Client;
import com.founder.ark.ids.bean.keycloak.Group;
import com.founder.ark.ids.service.GroupService;
import com.founder.ark.ids.util.PollResult;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController(value = "kcGroupController")
@RequestMapping("/kc/admin")
public class GroupController {

    @Autowired
    GroupService service;
    @Autowired
    private GroupDao groupDao;

    private static Logger logger = LoggerFactory.getLogger(GroupController.class);

    private ResponseObject check(@RequestBody @Valid Group group, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            for (ObjectError error :
                    errors) {
                if (ConstantsLibrary.Message.GROUP_NAME_EMPTY.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_NAME_EMPTY, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.GROUP_NAME_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_NAME_ILLEGLE, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.GROUP_DESC_LENGTH.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_DESC_LENGTH, error.getDefaultMessage());
                }
            }
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }

        return null;
    }


    /**
     * 新建组
     **/
    @RequestMapping(value = "/groups", method = RequestMethod.POST)
    public ResponseObject create(@RequestBody @Valid Group group, BindingResult bindingResult) {
        ResponseObject error = check(group, bindingResult);
        if (error != null) {
            return error;
        }
        ResponseObject poll = poll(group);
        //判断groupType的值是否正确
        if (!group.getGroupType().equals("normal") && !group.getGroupType().equals("everyone")) {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUPTYPE_ERROR, ConstantsLibrary.Message.GROUPTYPE_ERROR);
        }
        if (poll.getStatus() == 0) {
            return service.createOrUpdate(group);
        }
        return poll;
    }

    /**
     * 获取所有组信息
     */
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public ResponseObject getGroups(Integer pageNumber, Integer pageSize, String groupName) {
        //首先验证pageNumber和pageSize的合法性
        if (pageNumber != null) {
            if (pageNumber < 1) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.Invalid_PageNumber, ConstantsLibrary.Message.Invalid_PageNumber);
            }
        } else {
            //default value
            pageNumber = 1;
        }
        if (pageSize != null) {
            if (pageSize < 1 || pageSize > 9999) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.Invalid_PageSize, ConstantsLibrary.Message.Invalid_PageSize);
            }
        } else {
            //default value
            pageSize = 20;
        }

        PageData<Group> groups = service.getGroups(pageNumber, pageSize, groupName);
        logger.info("获取所有组信息");
        return ResponseObject.newSuccessResponseObject(groups, ConstantsLibrary.Message.SUCCESS);
    }

    /**
     * 查看特定组信息
     */
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.GET)
    public ResponseObject getGroupInfo(@PathVariable String id) {
        try {
            GroupRepresentation group = service.getGroupInfo(id);
            return ResponseObject.newSuccessResponseObject(group, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getGroupInfo Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }

    }

    /**
     * 查看组内成员
     */
    @GetMapping(value = "groups/{id}/users/inner")
    public ResponseObject getUsersInGroup(@PathVariable String id) {
        PageData<UserRepresentation> usersInGroup = new PageData<UserRepresentation>();
        try {
            usersInGroup = service.usersInGroup(id);
            return ResponseObject.newSuccessResponseObject(usersInGroup, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getUsersInGroup Exception", e);
            return ResponseObject.newErrorResponseObject(-1, "未知错误。");
        }
    }

    /**
     * 查看组外成员
     */
    @GetMapping(value = "groups/{id}/users/external")
    public ResponseObject getUsersNotInThisGroup(@PathVariable String id) {
        PageData<UserRepresentation> usersInGroup = new PageData<UserRepresentation>();
        try {
            usersInGroup = service.usersNotInGroup(id);
            return ResponseObject.newSuccessResponseObject(usersInGroup, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getUsersNotInThisGroup Exception", e);
            return ResponseObject.newErrorResponseObject(-1, "未知错误。");
        }
    }

    /**
     * 查看组内应用
     */
    @GetMapping(value = "groups/{id}/apps/inner")
    public ResponseObject getAppsInGroup(@PathVariable String id) {
        PageData<Client> clientsInGroup;
        try {
            clientsInGroup = service.innerClients(id);
            return ResponseObject.newSuccessResponseObject(clientsInGroup, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getAppsInGroup Exception", e);
            return ResponseObject.newErrorResponseObject(-1, "未知错误。");
        }
    }

    /**
     * 查看组外应用
     */
    @GetMapping(value = "groups/{id}/apps/external")
    public ResponseObject getAppsOutGroup(@PathVariable String id) {
        PageData<Client> clientsOutGroup;
        try {
            clientsOutGroup = service.externalClients(id);
            return ResponseObject.newSuccessResponseObject(clientsOutGroup, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getAppsOutGroup Exception", e);
            return ResponseObject.newErrorResponseObject(-1, "未知错误。");
        }
    }

    /**
     * 删除组
     */
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.DELETE)
    public ResponseObject deleteGroup(@PathVariable String id) {
        //判断所删除的组是不是所有人组，假如是所有人组，则不能被删除
        Optional<Group> groupThatWillBeDelete = groupDao.findById(id);
        if (groupThatWillBeDelete != null && groupThatWillBeDelete.get().getGroupType().equals("everyone")) {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_DELETE_ERROR, ConstantsLibrary.Message.GROUP_DELETE_ERROR);
        }
        logger.info("执行删除组："+groupThatWillBeDelete.get().getGroupName());
        try {
            service.deleteGroup(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("delete Exception", e);
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_DELETE_ERROR, ConstantsLibrary.Message.GROUP_DELETE_ERROR);
        }
    }

    /**
     * 批量删除组
     */
    @RequestMapping(value = "/groups/actions/delete", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchDeleteGroups(@RequestBody @Valid GroupController.IDS ids) {
        Boolean errorFlag = false;
        for (String id : ids.getIds()) {
            //判断所删除的组是不是所有人组，假如是所有人组，则不能被删除
            Optional<Group> groupThatWillBeDelete = groupDao.findById(id);
            if (groupThatWillBeDelete != null && groupThatWillBeDelete.get().getGroupType().equals("everyone")) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_BATCH_DELETE_ERROR, ConstantsLibrary.Message.GROUP_BATCH_DELETE_ERROR);
            }
            try {
                service.deleteGroup(id);
            } catch (Exception e) {
                logger.error("batchDeleteGroups Exception", e);
                errorFlag = true;
            }
        }
        if (errorFlag == false) {
            Integer count = ids.getIds().size();
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } else {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUP_BATCH_DELETE_ERROR, ConstantsLibrary.Message.GROUP_BATCH_DELETE_ERROR);
        }


    }

    /**
     * 更新组信息
     */
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject updateGroup(@PathVariable String id, @RequestBody @Valid Group group, BindingResult bindingResult) {
        ResponseObject error = check(group, bindingResult);
        if (error != null) {
            return error;
        }
        group.setId(id);
        //校验组名称是否已经存在
        Group groupThatWillBeDelete = groupDao.findByGroupName(group.getGroupName());
        if (groupThatWillBeDelete != null && !groupThatWillBeDelete.getId().equals(id)) {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUPNAME_OCCUPIED, ConstantsLibrary.Message.GROUPNAME_OCCUPIED);
        }
        //判断groupType的值是否正确
        if (!group.getGroupType().equals("normal") && !group.getGroupType().equals("everyone")) {
            return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.GROUPTYPE_ERROR, ConstantsLibrary.Message.GROUPTYPE_ERROR);
        }
        try {
            return service.createOrUpdate(group);
        } catch (Exception e) {
            logger.error("updateGroup Exception", e);
            return ResponseObject.newErrorResponseObject(1151, "更新组失败。");
        }
    }


    /**
     * 封装了批量删除时提交的body类
     */
    private static class IDS {
        List<String> ids;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }
    }


    /**
     * 组名称唯一性校验
     */
    @RequestMapping(value = "/groups/actions/poll", method = RequestMethod.POST)
    public ResponseObject poll(@RequestBody Group group) {
        synchronized (this.getClass()) {
            PollResult pollResult = service.poll(group);
            if (pollResult.isOccupied()) {
                return ResponseObject.newErrorResponseObject(pollResult.getStatus(), pollResult.toString());
            } else {
                return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
            }
        }
    }
}

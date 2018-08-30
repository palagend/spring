package com.founder.ark.ids.admin.controller;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.bean.keycloak.Client;
import com.founder.ark.ids.bean.keycloak.Group;
import com.founder.ark.ids.service.ClientService;
import com.founder.ark.ids.util.PollResult;
import io.swagger.annotations.Api;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("kc/admin")
@Api(tags = "IDS管理系统API")
public class ClientController {
    @Autowired
    private ClientService service;

    private static Logger logger = LoggerFactory.getLogger(ClientController.class);

    /**
     * 新建应用*/
    @RequestMapping(value = "/apps", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject create(@Valid @RequestBody Client client, BindingResult bindingResult) {
        ResponseObject responseObject = checkBindingResults(bindingResult);
        if(responseObject!=null){
            return responseObject;
        }
        return service.createOrUpdate(client);
    }


    /**获取应用列表或检索应用*/
    @RequestMapping(value = "/apps", method = RequestMethod.GET)
    public ResponseObject getClients(Integer pageNumber, Integer pageSize, String appName) {
        if (pageNumber != null && pageNumber < 0) {
            return ResponseObject.newErrorResponseObject(1001, ConstantsLibrary.Message.Invalid_PageNumber);
        } else if (pageSize != null && (pageSize < 0 || pageSize > 9999)) {
            return ResponseObject.newErrorResponseObject(1002, ConstantsLibrary.Message.Invalid_PageSize);
        }
        try {
            pageNumber = (pageNumber == null) ? 1 : pageNumber;
            pageSize = (pageSize == null) ? 20 : pageSize;
            PageData pageData = service.getClients(pageNumber, pageSize, appName);
            return ResponseObject.newSuccessResponseObject(pageData, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getClients Exception", e);
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }
    }


    /**删除应用*/
    @RequestMapping(value = "/apps/{id}", method = RequestMethod.DELETE)
    public ResponseObject delete(@PathVariable String id) {
        try {
            service.delete(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("delete Exception", e);
            return ResponseObject.newErrorResponseObject(1270, ConstantsLibrary.Message.CLIENT_DELETED_FAILED);
        }

    }

    /**获取应用配置信息*/
    @RequestMapping(value = "/apps/{id}/configuration", method = RequestMethod.GET)
    public ResponseObject<Client> getConfigOfClient(@PathVariable String id) {
        try {
            Client client = service.getConfigOfClient(id, false);
            return ResponseObject.newSuccessResponseObject(client, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getConfigOfClient Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    /**获取应用全部信息（包含所分配的组信息）*/
    @RequestMapping(value = "/apps/{id}", method = RequestMethod.GET)
    public ResponseObject<Client> getAllAppInfo(@PathVariable String id) {
        try {
            Client client = service.getConfigOfClient(id, true);
            return client == null ? ResponseObject.newErrorResponseObject(-1, "The app doesn't exist.") : ResponseObject.newSuccessResponseObject(client, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getAllAppInfo Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    /**修改应用的配置信息*/
    @RequestMapping(value = "/apps/{id}/configuration", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject updateConfig(@PathVariable String id, @RequestBody @Valid Client client,BindingResult bindingResult) {
        ResponseObject responseObject = checkBindingResults(bindingResult);
        if(responseObject!=null){
            return responseObject;
        }
        client.setId(id);
        try {
            service.createOrUpdate(client);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("createOrUpdate Exception", e);
            if(e.getMessage().equals("HTTP 409 Conflict")){
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.CLIENT_NAME_OCCUPIED,ConstantsLibrary.Message.CLIENT_NAME_OCCUPIED);
            }
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    /**更新应用全部信息（包含组）*/
    @RequestMapping(value = "/apps/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject updateAllInfo(@PathVariable String id, @RequestBody @Valid ClientPlus clientPlus,BindingResult bindingResult) {
        ResponseObject responseObject = checkBindingResults(bindingResult);
        if(responseObject!=null){
            return responseObject;
        }
        Client client = new Client(id, clientPlus.getAppName(), clientPlus.getIconUrl(), clientPlus.getLoginUrl());
        try {
            service.createOrUpdate(client);
            service.updateAssigendGroups(clientPlus.getAssignedGroups(), id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("createOrUpdate Exception", e);
            if("HTTP 409 Conflict".equals(e.getMessage())){
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.CLIENT_NAME_OCCUPIED,ConstantsLibrary.Message.CLIENT_NAME_OCCUPIED);
            }
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }


    /**获取分配该应用的组信息*/
    @RequestMapping(value = "/apps/{id}/groups/assigned", method = RequestMethod.GET)
    public ResponseObject<List<Group>> getAssignedGroups(@PathVariable String id) {
        try {
            List<Group> list = service.getAssignedGroups(id);
            return ResponseObject.newSuccessResponseObject(list, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getAssignedGroups Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    /**获取未分配该应用的组信息*/
    @RequestMapping(value = "/apps/{id}/groups/unassigned", method = RequestMethod.GET)
    public ResponseObject<List<Group>> getUnassignedGroups(@PathVariable String id) {
        try {
            List<Group> list = service.getUnassignedGroups(id);
            return ResponseObject.newSuccessResponseObject(list, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getUnassignedGroups Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    /**更新分配该应用的组信息*/
    @RequestMapping(value = "/apps/{id}/groups/assigned", method = RequestMethod.PUT)
    public ResponseObject updateAssigendGroups(@PathVariable String id, @RequestBody GroupIds groupIds) {
        try {
            service.updateAssigendGroups(groupIds.getIds(), id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("updateAssignedGroups Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    /**
     * 应用名称的唯一性校验
     */
    @RequestMapping(value = "/apps/actions/poll", method = RequestMethod.POST)
    public ResponseObject poll(@RequestBody Client client) {
        synchronized (this.getClass()) {
            PollResult pollResult = service.poll(client);
            if (pollResult.isOccupied()) {
                return ResponseObject.newErrorResponseObject(pollResult.getStatus(), pollResult.toString());
            } else {
                return ResponseObject.newSuccessResponseObject(pollResult.toString());
            }
        }
    }

    public ResponseObject checkBindingResults(BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            List<ObjectError> errors = bindingResult.getAllErrors();
            for (ObjectError error :
                    errors) {
                if (ConstantsLibrary.Message.CLIENT_NAME_NOT_NULL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.CLIENT_NAME_NOT_NULL, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.CLIENT_NAME_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.CLIENT_NAME_ILLEGAL, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.ICON_URL_NOT_NULL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.ICON_URL_NOT_NULL, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.LOGIN_URL_NOT_NULL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.LOGIN_URL_NOT_NULL, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.LOGIN_URL_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.LOGIN_URL_ILLEGAL, error.getDefaultMessage());
                }
            }
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }
        return null;
    }

    /**
     * 封装了批量删除时提交的body类
     */
    private static class GroupIds {
        List<String> ids;

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }
    }

    private static class ClientPlus {
        @NotNull(message = ConstantsLibrary.Message.CLIENT_NAME_NOT_NULL)
        @Length(max = 50, message = ConstantsLibrary.Message.CLIENT_NAME_ILLEGAL)
        String appName;

        @NotNull(message = ConstantsLibrary.Message.ICON_URL_NOT_NULL)
        String iconUrl;

        @NotNull(message = ConstantsLibrary.Message.LOGIN_URL_NOT_NULL)
        @Pattern(regexp = "(http|https):\\/\\/([\\w.]+\\/?)\\S*",message = ConstantsLibrary.Message.LOGIN_URL_ILLEGAL)
        String loginUrl;

        List<String> assignedGroups;

        public String getLoginUrl() {
            return loginUrl;
        }

        public void setLoginUrl(String loginUrl) {
            this.loginUrl = loginUrl;
        }


        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public List<String> getAssignedGroups() {
            return assignedGroups;
        }

        public void setAssignedGroups(List<String> assignedGroups) {
            this.assignedGroups = assignedGroups;
        }
    }

}


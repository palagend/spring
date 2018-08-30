package com.founder.ark.ids.avatar.controller;

import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.Athena;
import com.founder.ark.ids.avatar.representations.All;
import com.founder.ark.ids.avatar.representations.IDList;
import com.founder.ark.ids.avatar.representations.Post;
import com.founder.ark.ids.avatar.representations.UserWrapper;
import com.founder.ark.ids.avatar.service.KeycloakFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huyh (mailto:huyh@founder.com)
 */
@RestController
@RequestMapping("/api/v2")
@Slf4j
@Api(tags = "用户管理API", description = "通过该API能够管理keycloak上面的用户")
public class AvatarUserController {
    @Autowired
    private KeycloakFacade keycloakFacade;
    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @ApiOperation("唯一性校验 1006:用户名被占用 1017:邮箱被占用")
    @GetMapping("/users/actions/poll")
    public ResponseObject poll(@RequestParam(required = false) String username, @RequestParam(required = false) String email) {
        if (username == null && email == null) {
            return ResponseObject.newSuccessResponseObject(null);
        }
        if (username != null) {
            if (!keycloakFacade.isUsernameVacant(username))
                return ResponseObject.newErrorResponseObject(1006, messageSource.getMessage("avatar.validation.unique.username", null, LocaleContextHolder.getLocale()));
        }
        if (email != null) {
            if (!keycloakFacade.isEmailVacant(email))
                return ResponseObject.newErrorResponseObject(1017, messageSource.getMessage("avatar.validation.unique.email", null, LocaleContextHolder.getLocale()));
        }
        return ResponseObject.newSuccessResponseObject(null);
    }

    @ApiOperation("重置用户密码")
    @GetMapping(value = "/users/{id}/actions/reset-password")
    public ResponseObject resetPassword(@PathVariable String id) {
        keycloakFacade.resetPassword(id);
        return ResponseObject.newSuccessResponseObject(null);
    }

    @ApiOperation("批量重置用户密码")
    @RequestMapping(value = "/users/actions/batch-reset-password", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchResetPassword(@RequestBody IDList idList) {
        for (String id : idList.getIds()) {
            resetPassword(id);
        }
        return ResponseObject.newSuccessResponseObject(null);
    }

    @ApiOperation("批量禁用用户")
    @RequestMapping(value = "/users/actions/disable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchDisableUsers(@RequestBody IDList idList) {
        for (String id : idList.getIds()) {
            if (log.isTraceEnabled()) log.trace("enabling user: id is ({})", id);
            keycloakFacade.disable(id);
        }
        return ResponseObject.newSuccessResponseObject(null);
    }

    @ApiOperation("批量激活用户")
    @PostMapping(value = "/users/actions/enable", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchEnableUsers(@RequestBody IDList idList) {
        for (String id : idList.getIds()) {
            if (log.isTraceEnabled()) log.trace("enabling user: id is ({})", id);
            keycloakFacade.enable(id);
        }
        return ResponseObject.newSuccessResponseObject(null);
    }

    @ApiOperation("更新用户信息")
    @PutMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject update(@RequestBody @Validated({All.class}) UserWrapper user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError e : bindingResult.getAllErrors()) {
                sb.append(e.getDefaultMessage()).append(" ");
            }
            return ResponseObject.newErrorResponseObject(1030, sb.toString().trim());
        }
        keycloakFacade.update(user);
        return ResponseObject.newErrorResponseObject(-1, messageSource.getMessage("avatar.system.error.message", null, LocaleContextHolder.getLocale()));
    }

    @PostMapping(value = "/users/actions/delete", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("批量删除用户")
    public ResponseObject batchDeleteUsers(@RequestBody IDList idList) {
        for (String id : idList.getIds()) {
            if (log.isTraceEnabled()) log.trace("deleting user: id is ({})", id);
            delete(id);
        }
        return ResponseObject.newSuccessResponseObject(null);
    }

    @ApiOperation("删除用户")
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    public ResponseObject delete(@PathVariable String id) {
        Response resp = keycloakFacade.delete(id);
//        if (resp.getStatus() == HttpServletResponse.SC_NO_CONTENT)
        if (resp.getStatus() >= HttpServletResponse.SC_OK && resp.getStatus() < HttpServletResponse.SC_MULTIPLE_CHOICES)
            return ResponseObject.newSuccessResponseObject(resp.getStatusInfo());
        return ResponseObject.newErrorResponseObject(resp.getStatus(), resp.getStatusInfo().toString());
    }

    @ApiOperation("获取符合过滤条件的用户并分页展示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNumber", value = "页码，第一页从1开始，默认是1"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数，默认是20"),
            @ApiImplicitParam(name = "searchString", value = "模糊搜索的字符串，可以匹配用户名，邮箱")
    })
    @GetMapping(value = "/users")
    public List<UserWrapper> getUsers(
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            String searchString) {
        pageNumber = pageNumber < 1 ? 1 : pageNumber;
        pageSize = (pageSize < 1 && pageSize > Athena.MAX_RESULTS) ? 20 : pageSize;
        PagedListHolder<UserRepresentation> page = keycloakFacade.list(searchString);
        page.setPageSize(pageSize);
        page.setPage(pageNumber - 1);
        List<UserRepresentation> lst = page.getPageList();
        List<UserWrapper> result = new ArrayList<>();
        for (UserRepresentation u : lst) {
            result.add(UserWrapper.wrap(u));
        }
        return result;
    }

    @ApiOperation("新建用户")
    @PostMapping(value = "/users")
    public ResponseObject create(@RequestBody @Validated({Post.class}) UserWrapper user, BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        if (bindingResult.hasErrors()) {
            for (ObjectError e : bindingResult.getAllErrors()) {
                sb.append(e.getDefaultMessage()).append(" ");
            }
            return ResponseObject.newErrorResponseObject(-1, sb.toString().trim());
        }
        Response response = keycloakFacade.createOrUpdate(user);
        if (response.getStatus() == HttpServletResponse.SC_CREATED) {
            return ResponseObject.newSuccessResponseObject(response.getStatusInfo());
        }
        return ResponseObject.newErrorResponseObject(response.getStatus(), response.getStatusInfo().toString());
    }


    @ApiOperation("根据用户名查找特定用户的信息")
    @ApiImplicitParams(@ApiImplicitParam(name = "username", value = "用户的用户名"))
    @GetMapping(value = "/users/username/{username}")
    public ResponseObject getUserInfoByUsername(@PathVariable String username) {
        return ResponseObject.newSuccessResponseObject(keycloakFacade.findByUsername(username));
    }

    @ApiOperation("查看特定用户信息")
    @ApiImplicitParams(@ApiImplicitParam(name = "id", value = "用户的id"))
    @GetMapping(value = "/users/id/{id}")
    public ResponseObject getUserInfo(@PathVariable String id) {
        UserWrapper user = keycloakFacade.detail(id);
        return ResponseObject.newSuccessResponseObject(user);
    }
}

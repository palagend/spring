package com.founder.ark.ids.avatar.controller;

import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.avatar.representations.GroupWrapper;
import com.founder.ark.ids.avatar.service.KeycloakFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * IDS组管理
 *
 * @author 胡月恒
 * @mail huyh@founder.com
 * @date 2018-08-21
 */
@RestController
@RequestMapping("/api/v2/groups")
@Api(tags = "群组管理API", description = "通过群组管理API可以管理Keycloak上面的组")
public class AvatarGroupController {
    @Autowired
    KeycloakFacade keycloakFacade;

    @ApiOperation("获取符合过滤条件的组并分页展示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNumber", value = "页码，第一页从1开始，默认是1"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数，默认是20"),
            @ApiImplicitParam(name = "groupName", value = "模糊搜索的组名")
    })
    @GetMapping("/")
    List<GroupWrapper> groups(@RequestParam(required = false, defaultValue = "1") int pageNumber,
                              @RequestParam(required = false, defaultValue = "20") int pageSize,
                              @RequestParam(required = false) String groupName) {
        PagedListHolder page = keycloakFacade.groups(groupName);
        page.setPage(pageNumber - 1);
        page.setPageSize(pageSize);
        List<GroupRepresentation> lst = page.getPageList();
        List<GroupWrapper> result = new ArrayList<>();
        for (GroupRepresentation u : lst) {
            result.add(GroupWrapper.wrap(u));
        }
        return result;
    }

    @ApiOperation("新建组")
    @PostMapping(value = "/")
    public ResponseObject create(@RequestBody @Valid GroupWrapper group, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError e : bindingResult.getAllErrors()) {
                sb.append(e.getDefaultMessage()).append(" ");
            }
            return ResponseObject.newErrorResponseObject(1031, sb.toString().trim());
        }
        Response response = keycloakFacade.createOrUpdate(group);
        if (response.getStatus() == HttpServletResponse.SC_CREATED)
            return ResponseObject.newSuccessResponseObject(response.getStatusInfo());
        return ResponseObject.newErrorResponseObject(response.getStatus(), response.getStatusInfo().toString());
    }

    @ApiOperation("查看组详情")
    @GetMapping("/{id}")
    public ResponseObject<GroupWrapper> detail(@PathVariable String id) {
        GroupWrapper group = keycloakFacade.groupDetail(id);
        return ResponseObject.newSuccessResponseObject(group);
    }

    @ApiOperation("编辑组信息")
    @PutMapping("/")
    public ResponseObject edit(@RequestBody @Validated GroupWrapper group, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            for (ObjectError e : bindingResult.getAllErrors()) {
                sb.append(e.getDefaultMessage()).append(" ");
            }
            return ResponseObject.newErrorResponseObject(1031, sb.toString());
        }
        Response resp = keycloakFacade.createOrUpdate(group);
        if (resp.getStatus() == HttpServletResponse.SC_OK)
            return ResponseObject.newSuccessResponseObject(resp.getStatusInfo());
        return ResponseObject.newErrorResponseObject(resp.getStatus(), resp.getStatusInfo().toString());

    }

    @ApiOperation("删除组")
    @DeleteMapping("/{id}")
    public ResponseObject delete(@PathVariable String id) {
        keycloakFacade.groupDelete(id);
        return ResponseObject.newSuccessResponseObject(null);
    }
}
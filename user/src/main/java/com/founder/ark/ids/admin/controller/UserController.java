package com.founder.ark.ids.admin.controller;

import com.founder.ark.common.utils.bean.PageData;
import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.admin.dao.UserDao;
import com.founder.ark.ids.bean.ConstantsLibrary;
import com.founder.ark.ids.bean.keycloak.User;
import com.founder.ark.ids.service.UserService;
import com.founder.ark.ids.util.BeanUtils;
import com.founder.ark.ids.util.PollResult;
import com.founder.ark.ids.util.StringHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Range;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * @author huyh (mailto:huyh@founder.com)
 */
@RestController(value = "kcUserController")
@RequestMapping("/kc/admin")
@Validated
@Api(tags = "Admin用户管理API")
public class UserController {
    @Autowired
    UserService service;
    @Autowired
    private UserDao dao;

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    private UserRepresentation parseUserRepresentation(User user) {
        UserRepresentation urep = new UserRepresentation();
        urep.setUsername(user.getUsername());
        urep.setEmail(user.getEmail());
//        urep.setFirstName(user.getFirstName());
        Map<String, String> XM = StringHelper.parseName(user.getFirstName());
        urep.setFirstName(XM.get(StringHelper.GIVEN_NAME));
        logger.info("given name:" + XM.get(StringHelper.GIVEN_NAME));
        urep.setEnabled(user.getEnabled());
        urep.setGroups(user.getGroups());
        urep.setRealmRoles(user.getRoles());
//        urep.setLastName(user.getFirstName());//firstname和lastname统一为firstname
        urep.setLastName(XM.get(StringHelper.FAMILY_NAME));
        logger.info("family name:" + XM.get(StringHelper.FAMILY_NAME));
        Map<String, List<String>> map = new HashMap<>();
        map.put("mobilePhone", Arrays.asList(user.getMobilePhone()));
        map.put("company", Arrays.asList(user.getCompany()));
        urep.setAttributes(map);
        return urep;
    }

    private ResponseObject check(@RequestBody @Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            for (ObjectError error :
                    errors) {
                if (ConstantsLibrary.Message.USERNAME_NULL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USERNAME_EMPTY, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.USERNAME_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USERNAME_ILLEGLE, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.EMAIL_NULL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.EMAIL_NULL, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.EMAIL_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.EMAIL_ILLEGLE, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.NAME_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.NAME_ILLEGLE, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.NAME_EMPTY.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.NAME_EMPTY, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.MOBILE_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.MOBILE_ILLEGLE, error.getDefaultMessage());
                }
            }
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }
        if (user != null) {
            synchronized (this.getClass()) {
                PollResult pollResult = service.poll(user);
                if (pollResult.isOccupied()) {
                    return ResponseObject.newErrorResponseObject(pollResult.getStatus(), pollResult.toString());
                }
            }
        }
        return null;
    }

    //唯一性校验
    @RequestMapping(value = "/users/actions/poll", method = RequestMethod.POST)
    public ResponseObject poll(@RequestBody User user) {
        synchronized (this.getClass()) {
            PollResult pollResult = service.poll(user);
            if (pollResult.isOccupied()) {
                return ResponseObject.newErrorResponseObject(pollResult.getStatus(), pollResult.toString());
            } else {
                return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
            }
        }
    }

    //新建用户
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ResponseObject create(@RequestBody @Valid User user, BindingResult bindingResult) {
        ResponseObject error = check(user, bindingResult);
        if (error != null) {
            return error;
        }
        ResponseObject resp = service.create(parseUserRepresentation(user), user.getTemporary());
        return resp;
    }

    @ApiOperation(value = "根据用户名查找特定用户的信息")
    @RequestMapping(value = "/users/findByUsername/{username:^[\\w\\W]{0,50}$}", method = RequestMethod.GET)
    public ResponseObject getUserInfoByUsername(@PathVariable String username) {
        try {
//            UserRepresentation user = service.getUserInfo(id);


            User user = dao.findByUsername(username);
            return ResponseObject.newSuccessResponseObject(user, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getUserInfoByUsername Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }


    //查看特定用户信息
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public ResponseObject getUserInfo(@PathVariable String id) {
        try {
//            UserRepresentation user = service.getUserInfo(id);
            Optional<User> user = dao.findById(id);
            return ResponseObject.newSuccessResponseObject(user.get(), ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("getUserInfo Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    //重置用户密码
    @RequestMapping(value = "/users/{id}/actions/reset-password", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject resetPassword(@PathVariable String id) {
        try {
            service.resetPassword(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("resetPassword Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }

    }

    //批量重置用户密码
    @RequestMapping(value = "/users/actions/batch-reset-password", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchResetPassword(@RequestBody IDS ids) {
        for (String id : ids.getIds()) {
            service.resetPassword(id);
        }
        return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
    }

    //批量禁用用户
    @RequestMapping(value = "/users/actions/disable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchDisableUsers(@RequestBody @Valid IDS ids) {
        try {
            service.batchSwitch(ids.getIds(), false);
            Integer count = ids.getIds().size();
            return ResponseObject.newSuccessResponseObject(null, count + " 用户禁用成功");
        } catch (Exception e) {
            logger.error("batchDisableUsers Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }

    }

    //批量激活用户
    @RequestMapping(value = "/users/actions/enable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchEnableUsers(@RequestBody @Valid IDS ids) {
        try {
            service.batchSwitch(ids.getIds(), true);
            Integer count = ids.getIds().size();
            return ResponseObject.newSuccessResponseObject(null, count + " 用户激活成功");
        } catch (Exception e) {
            logger.error("batchEnableUsers Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }

    }

    //批量删除用户
    @RequestMapping(value = "/users/actions/delete", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject batchDeleteUsers(@RequestBody @Valid IDS ids) {
        for (String id : ids.getIds()) {
            Response resp = service.delete(id);
            if (resp.getStatus() == SC_OK || resp.getStatus() == SC_NO_CONTENT || resp.getStatus() == SC_NOT_FOUND) {
                dao.deleteById(id);
            } else {
                return ResponseObject.newErrorResponseObject(1050, "删除失败: \n" + resp.getStatusInfo().toString());
            }
        }
        return ResponseObject.newSuccessResponseObject(ConstantsLibrary.Message.SUCCESS);
    }


    //更新用户信息
    @RequestMapping(value = "/users/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseObject update(@PathVariable String id, @RequestBody @Valid UpdateBox box, BindingResult bindingResult) {
        ResponseObject errorResult = check(null, bindingResult);
        if (errorResult != null) {
            return errorResult;
        }
        User user = new User();
        BeanUtils.copyPropertiesIgnoreNull(box, user);
        synchronized (this.getClass()) {
            PollResult pollResult = service.poll(user);
            if (pollResult.isOccupied()) {
                return ResponseObject.newErrorResponseObject(pollResult.getStatus(), pollResult.toString());
            }
        }
        UserRepresentation urep = new UserRepresentation();

        Map<String, List<String>> map = new HashMap<>();
        map.put("mobilePhone", Arrays.asList(box.getMobilePhone()));
        urep.setAttributes(map);
        urep.setId(id);
        urep.setEmail(box.getEmail());
        String name = box.getFirstName();
        if (name != null) {
            Map<String, String> XM = StringHelper.parseName(name);
            urep.setFirstName(XM.get(StringHelper.GIVEN_NAME));
            urep.setLastName(XM.get(StringHelper.FAMILY_NAME));
        }
        try {
            service.update(urep);
            user = dao.findById(id).get();
            BeanUtils.copyPropertiesIgnoreNull(box, user);
            dao.save(user);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } catch (Exception e) {
            logger.error("update Exception", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }

    }

    //删除用户
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    public ResponseObject delete(@PathVariable String id) {

        Response resp = service.delete(id);
        if (resp.getStatus() == SC_OK || resp.getStatus() == SC_NO_CONTENT || resp.getStatus() == SC_NOT_FOUND) {
            dao.deleteById(id);
            return ResponseObject.newSuccessResponseObject(null, ConstantsLibrary.Message.SUCCESS);
        } else {
            return ResponseObject.newErrorResponseObject(1050, resp.getStatusInfo().toString());
        }
    }

    //获取所有用户信息
    @RequestMapping(value = "/users", method = RequestMethod.GET)
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
        PageData<User> users = service.users(pageNumber, pageSize, searchString);
        logger.info("获取所有用户信息");
        return ResponseObject.newSuccessResponseObject(users, ConstantsLibrary.Message.SUCCESS);
    }

    @RequestMapping(value = "/users/simple", method = RequestMethod.GET)
    public ResponseObject getSimpleUsers() {
        PageData<User> users = service.simpleLocalUser();
        logger.info("新建或编辑组时加载所有用户信息");
        return ResponseObject.newSuccessResponseObject(users, ConstantsLibrary.Message.SUCCESS);
    }

    //封装了所有需要进行验证的属性的类
    private static class UpdateBox {
        @Pattern(regexp = "(^((\\+86)|(86))?1([34578])\\d{9}$)|(^$)", message = ConstantsLibrary.Message.MOBILE_ILLEGAL)
        String mobilePhone;
        @Email(message = ConstantsLibrary.Message.EMAIL_ILLEGAL, regexp = StringHelper.EMAIL_REGEX)
        String email;
        @Pattern(regexp = StringHelper.NAME_REGEX, message = ConstantsLibrary.Message.NAME_ILLEGAL)
        String firstName;

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }

    //封装了请求用户列表时的参数信息
    private static class RequestParamsInGetUsersMethod {
        @Range(min = 1, message = ConstantsLibrary.Message.Invalid_PageNumber)
        Integer pageNumber;
        Integer pageSize;
        String username;
        String firstName;
        String email;
        String mobilePhone;
        String enabled;

        public Integer getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobilePhone() {
            return mobilePhone;
        }

        public void setMobilePhone(String mobilePhone) {
            this.mobilePhone = mobilePhone;
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }
    }

    //封装了提交新密码时的body类
    private static class PasswordBody {
        @Pattern(regexp = StringHelper.PASSWORD_REGEX, message = "密码长度不能少于8位，且必须包含英文字母和数字")
        String newPassword;
        String confirmPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }


        public String getPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public boolean checkConsistency() {
            return newPassword.equals(confirmPassword);
        }
    }

    //封装了激活/禁用时提交的body类
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

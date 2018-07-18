package com.founder.ark.ids.service.portal.user.controller;

import com.founder.ark.common.utils.bean.ResponseObject;
import com.founder.ark.ids.service.admin.user.controller.ClientController;
import com.founder.ark.ids.service.api.portal.UserService;
import com.founder.ark.ids.service.core.bean.ConstantsLibrary;
import com.founder.ark.ids.service.core.bean.keycloak.Client;
import com.founder.ark.ids.service.core.bean.keycloak.User;
import com.founder.ark.ids.service.core.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * Created by cheng.ly on 2018/3/21.
 */
@RestController
@RequestMapping("/kc/portal")
public class UserController {
    @Autowired
    UserService userService;

    private static Logger logger = LoggerFactory.getLogger(ClientController.class);

    @RequestMapping(value = "/user/pwd/{id}", method = RequestMethod.PUT)
    public ResponseObject updatePassword(@PathVariable String id, @RequestBody @Valid TempPwdEntity tempPassWord, BindingResult bindingResult) {
        ResponseObject responseObject = checkBindingResults(bindingResult);
        if (responseObject != null) {
            return responseObject;
        }
        try {
            return userService.updatePassword(id, tempPassWord.getOldPassword(), tempPassWord.getNewPassword(), tempPassWord.getConfirmPassword());
        } catch (Exception e) {
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }
    }

    @RequestMapping(value = "/user/info/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseObject getUserInfo(@PathVariable String id) {
        User user = userService.getUserInfo(id);
        return ResponseObject.newSuccessResponseObject(user, null);
    }

    @RequestMapping(value = "/user/apps/{id}", method = RequestMethod.GET)
    public ResponseObject getClientOfUser(@PathVariable String id) {
        try {
            List<Client> clientList = userService.getClientOfUser(id);
            return ResponseObject.newSuccessResponseObject(clientList, null);
        } catch (Exception e) {
            logger.error("Getting app throws exceptions.", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/user/id", method = RequestMethod.GET)
    public ResponseObject obtainUserId(@RequestHeader String authToken) {
        //通过HttpRequestHeader中的Token信息，获取请求ID的用户信息。
        try {
            Map map = userService.obtainUserId(authToken);
            if (map.size() == 0) {
                return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.USER_NOT_EXITED, ConstantsLibrary.Message.USER_NOT_EXITED);
            }
        } catch (Exception e) {
            logger.error("Getting userId throws exceptions.", e);
            return ResponseObject.newErrorResponseObject(-1, e.getMessage());
        }
        return ResponseObject.newSuccessResponseObject(userService.obtainUserId(authToken), null);
    }


    public static class TempPwdEntity {
        @NotNull
        @Pattern(regexp = StringHelper.PASSWORD_REGEX, message = ConstantsLibrary.Message.OLD_PASSWORD_INCORRECT)
        String oldPassword;
        @NotNull
        @Pattern(regexp = StringHelper.PASSWORD_REGEX, message = ConstantsLibrary.Message.PASSWORD_ILLEGAL)
        String newPassword;
        @NotNull
        @Pattern(regexp = StringHelper.PASSWORD_REGEX, message = ConstantsLibrary.Message.PASSWORD_ILLEGAL)
        String confirmPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }
    }

    public ResponseObject checkBindingResults(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            for (ObjectError error :
                    errors) {
                if (ConstantsLibrary.Message.OLD_PASSWORD_INCORRECT.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.OLD_PASSWORD_INCORRECT, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.PASSWORD_ILLEGAL.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.PASSWORD_ILLEGAL, error.getDefaultMessage());
                }
                if (ConstantsLibrary.Message.TWO_INPUTTED_CONFLICT.equals(error.getDefaultMessage())) {
                    return ResponseObject.newErrorResponseObject(ConstantsLibrary.StatusCode.TWO_INPUTTED_CONFLICT, error.getDefaultMessage());
                }
            }
            return ResponseObject.newErrorResponseObject(-1, "未知错误");
        }
        return null;
    }
}

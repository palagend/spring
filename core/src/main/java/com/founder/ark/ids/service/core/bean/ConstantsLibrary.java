package com.founder.ark.ids.service.core.bean;

public class ConstantsLibrary {
    public static final class Status {
        public static final int ENABLED = 1;
        public static final int DISABLED = 2;
        public static final int DELETED = 0;
    }

    public static final class Gender {
        public static final String MALE = "M";
        public static final String FEMALE = "F";
    }

    public static class StatusCode {
        public static final int Invalid_PageNumber = 1001;
        public static final int Invalid_PageSize = 1002;
        public static final int USERNAME_EMPTY = 1005;
        public static final int USERNAME_ILLEGLE = 1007;
        public static final int EMAIL_NULL = 1018;
        public static final int EMAIL_ILLEGLE = 1015;
        public static final int NAME_ILLEGLE = 1009;
        public static final int MOBILE_ILLEGLE = 1011;
        public static final int NAME_EMPTY = 1010;
        public static final int USERNAME_OCCUPIED = 1006;
        public static final int EMAIL_OCCUPIED = 1017;
        public static final int GROUP_NAME_EMPTY = 1105;
        public static final int GROUP_NAME_ILLEGLE = 1104;
        public static final int GROUP_DESC_LENGTH = 1109;
        public static final int GROUPNAME_OCCUPIED = 1106;
        public static final int MOBILE_OCCUPIED = 1014;
        public static final int GROUPTYPE_ERROR = 1110;
        public static final int GROUP_DELETE_ERROR = 1170;
        public static final int GROUP_BATCH_DELETE_ERROR = 1150;

        /**
         * 应用相关的错误码
         */
        public static final int CLIENT_NAME_OCCUPIED = 1206;
        public static final int CLIENT_NAME_NOT_NULL = 1205;
        public static final int CLIENT_NAME_ILLEGAL = 1204;
        public static final int ICON_URL_NOT_NULL = 1209;
        public static final int ICON_ILLEGAL = 1210;
        public static final int LOGIN_URL_NOT_NULL = 1215;
        public static final int LOGIN_URL_ILLEGAL = 1216;
        public static final int CLIENT_DELETED_FAILED = 1270;


        /**
         * 门户系统相关错误码
         */
        public static final  int USER_NOT_EXITED = 2060;
        public static final  int PASSWORD_ILLEGAL = 2050;
        public static final  int OLD_PASSWORD_INCORRECT = 2051;
        public static final  int TWO_INPUTTED_CONFLICT = 2052;

        /**
         * 会话管理相关错误码
         */
        public static final int SESSION_DELETE_ERROR = 1551;
        public static final int USER_BATCH_LOGOUT_ERROR = 1550;
        public static final int USER_LOGOUT_ERROR = 1560;

        /**
         * Token校验相关错误码
         */
        public static final  int USERNAME_OR_PASSWORD_INCORRECT = 1601;
        public static final  int TOKEN_INVALID = 1602;
    }

    public class Message {
        /**
         * 成功的返回信息,目前默认都为"成功"
         */
        public static final String SUCCESS = "操作成功。";
        public static final String USERNAME_NULL = "用户名不能为空。";
        public static final String USERNAME_ILLEGAL = "用户名格式非法。";
        public static final String MOBILE_ILLEGAL = "手机号格式不合法。";
        public static final String EMAIL_ILLEGAL = "邮箱格式非法。";
        public static final String EMAIL_NULL = "邮箱不能为空。";
        public static final String NAME_EMPTY = "姓名不能为空。";
        public static final String NAME_ILLEGAL = "姓名格式非法。";
        public static final String Invalid_PageNumber = "pageNumber为大于或等于1的整数";
        public static final String Invalid_PageSize = "pageSize为0到9999的整数";
        public static final String GROUP_NAME_EMPTY = "组名称不能为空。";
        public static final String GROUP_NAME_ILLEGAL = "组名称格式非法。";
        public static final String GROUP_DESC_LENGTH = "组描述过长。";
        public static final String GROUPNAME_OCCUPIED = "组名称已存在。";
        public static final String GROUPTYPE_ERROR = "组类型不对。";
        public static final String GROUP_DELETE_ERROR = "删除组失败。";
        public static final String GROUP_BATCH_DELETE_ERROR = "批量删除组失败。";


        /**
         * 应用相关的错误信息
         */
        public static final String CLIENT_NAME_OCCUPIED = "应用名称已存在。";
        public static final String CLIENT_NAME_ILLEGAL = "应用名称格式非法。";
        public static final String CLIENT_NAME_NOT_NULL = "应用名称不能为空。";
        public static final String ICON_URL_NOT_NULL = "图片地址不能为空。";
        public static final String ICON_ILLEGAL = "图片不符合格式要求。";
        public static final String CLIENT_DELETED_FAILED = "删除应用失败。";
        public static final String LOGIN_URL_NOT_NULL = "登录地址不能为空。";
        public static final String LOGIN_URL_ILLEGAL = "登录地址格式错误。";


        /**
         * 门户系统相关错误信息
         */
        public static final  String USER_NOT_EXITED = "用户不存在。";
        public static final  String PASSWORD_ILLEGAL = "新密码不符合规则。";
        public static final  String OLD_PASSWORD_INCORRECT = "旧密码不正确。";
        public static final  String TWO_INPUTTED_CONFLICT = "确认新密码与新密码不一致。";

        /**
         * 会话管理相关错误信息
         */
        public static final  String SESSION_DELETE_ERROR = "删除单条会话失败。";
        public static final  String USER_BATCH_LOGOUT_ERROR = "批量注销用户失败。";
        public static final  String USER_LOGOUT_ERROR = "注销用户失败。";

        /**
        * Token校验相关错误信息
        */
        public static final  String USERNAME_OR_PASSWORD_INCORRECT = "用户名或者密码错误。";
        public static final  String TOKEN_INVALID = "令牌失效。";





    }
}

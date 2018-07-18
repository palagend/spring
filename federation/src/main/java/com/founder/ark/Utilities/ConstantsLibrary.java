package com.founder.ark.Utilities;

public class ConstantsLibrary {
    public static class StatusCode {
        public static final int TEST_CONNECTION_ERROR = 1480;
        public static final int TEST_AUTHENTICATION_ERROR = 1490;
        public static final int AUTHENTICATION_SOURCE_NAME_TOO_LONG_ERROR = 1404;
        public static final int AUTHENTICATION_SOURCE_NAME_NULL_ERROR = 1405;
        public static final int AUTHENTICATION_SOURCE_NAME_CONFLICT_ERROR = 1406;
        public static final int USER_LDAP_FILTER_ILLEGAL = 1407;
        public static final int AUTHENTICATION_SOURCE_DELETE_ERROR = 1470;

    }

    public class Message {
        /**
         * 成功的返回信息,目前默认都为"成功"
         */
        public static final String SUCCESS = "操作成功。";
        public static final String TEST_CONNECTION_ERROR = "测试连接失败。";
        public static final String TEST_AUTHENTICATION_ERROR = "测试验证失败。";
        public static final String AUTHENTICATION_SOURCE_NAME_TOO_LONG_ERROR = "认证源名称过长。";
        public static final String AUTHENTICATION_SOURCE_NAME_NULL_ERROR = "认证源名称不能为空。";
        public static final String AUTHENTICATION_SOURCE_NAME_CONFLICT_ERROR = "认证源名称已存在。";
        public static final String AUTHENTICATION_SOURCE_DELETE_ERROR = "删除认证源失败。";
        public static final String USER_LDAP_FILTER_ILLEGAL = "userLDAPFilter如果非空，必须包含在()里。";
        public static final String FEDERATION_DEFAULT_VALUE = "{\"pagination\":[\"false\"],\"fullSyncPeriod\":[\"-1\"]," +
                "\"connectionPooling\":[\"true\"],\"cachePolicy\":[\"DEFAULT\"],\"useKerberosForPasswordAuthentication\":[\"false\"]," +
                "\"importEnabled\":[\"true\"],\"enabled\":[\"true\"],\"changedSyncPeriod\":[\"-1\"],\"allowKerberosAuthentication\":[\"false\"]," +
                "\"debug\":[\"false\"],\"searchScope\":[\"1\"],\"useTruststoreSpi\":[\"ldapsOnly\"],\"priority\":[\"0\"],\"editMode\":[\"READ_ONLY\"]," +
                "\"validatePasswordPolicy\":[\"false\"],\"batchSizeForSync\":[\"1000\"],\"syncRegistrations\":[\"false\"]}";
        }
}

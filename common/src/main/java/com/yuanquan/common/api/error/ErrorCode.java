package com.yuanquan.common.api.error;

import com.yuanquan.common.api.error.ErrorResult;
import com.yuanquan.common.LanguageUtils;

public enum ErrorCode {
    //    SUCCESS(0, "成功"),
    INVALID_TOKEN(-1, LanguageUtils.optString("TOKEN无效")),
    BIZ_EXCEPTION(1000, LanguageUtils.optString("业务异常")),
    SYSTEM_EXCEPTION(500, LanguageUtils.optString("系统异常")),
    ACCOUNT_TYPE_NOT_SUPPORT(1101, LanguageUtils.optString("账户类型不支持")),
    ACCOUNT_NOT_EXISTED(1102, LanguageUtils.optString("该账号不存在，请确认后再重试")),
    ACCOUNT_EXISTED(1103, LanguageUtils.optString("账户已存在")),
    ACCOUNT_DISABLED(1104, LanguageUtils.optString("账户状态禁用")),
    PASSWORD_NOT_CORRECT(1105, LanguageUtils.optString("密码不正确，请重试")),
    SMS_ERROR_CODE(1106, LanguageUtils.optString("验证码不正确")),
    OAUTH_ERROR_CODE(1107, LanguageUtils.optString("三方登录未注册")),
    PASSWORD_REQUIRED(1108, LanguageUtils.optString("密码不能为空")),
    ACCOUNT_ID_REQUIRED(1109, LanguageUtils.optString("账户id不能为空")),
    USER_NOT_EXISTED(2102, LanguageUtils.optString("用户不存在")),
    USER_EXISTED(2103, LanguageUtils.optString("用户已存在")),
    USER_DISABLED(2104, LanguageUtils.optString("用户状态禁用")),
    ORIGINAL_PASSWORD(2106, LanguageUtils.optString("原密码不正确"));

    public int code;
    public String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(ErrorResult errorResult) {
        if (errorResult.getCode() == 0 || (errorResult.getCode() == 1000 && errorResult.getErrMsg() != null && !errorResult.getErrMsg().isEmpty())) {
            return errorResult.getErrMsg();
        }
        for (ErrorCode value : values()) {
            if (value.code == errorResult.getCode()) {
                return value.message;
            }
        }
        return errorResult.getErrMsg();
    }
}

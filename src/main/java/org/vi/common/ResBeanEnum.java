package org.vi.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Eric Tseng
 * @description 公共返回对象枚举
 * @since 2022/2/19 15:53
 */
@Getter
@ToString
@AllArgsConstructor
public enum ResBeanEnum {
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),
    // 登录模块
    LOGIN_ERROR(500210, "用户名或密码不正确"),
    BIND_ERROR(500212, "参数校验异常"),
    USER_ERROR(500212, "用户不存在，请重新登录"),
    SIGNUP_ERROR(500212, "该手机号已被注册"),
    TOKEN_ERROR(500212, "无token，请重新登录");
    private final Integer code;
    private final String message;
}

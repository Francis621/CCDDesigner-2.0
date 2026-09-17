package com.ccdd.iam.dto;

import java.io.Serializable;

/**
 * 用户登录响应 DTO
 */
public class LoginResponse implements Serializable {

    private String token;
    private UserDetailDto user;
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, UserDetailDto user, Long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDetailDto getUser() {
        return user;
    }

    public void setUser(UserDetailDto user) {
        this.user = user;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

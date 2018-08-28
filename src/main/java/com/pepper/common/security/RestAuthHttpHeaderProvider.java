package com.pepper.common.security;

import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:基于spring-security 的权限 header provider
 */
public class RestAuthHttpHeaderProvider {

    private String user;
    private String password;

    public RestAuthHttpHeaderProvider(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public org.apache.http.Header getHeaders() {
        String username = this.user;
        String password = this.password;
        Header headers = new BasicHeader(HttpHeaders.AUTHORIZATION, encode(username, password));
        return headers;
    }

    protected String encode(String username, String password) {
        String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

}
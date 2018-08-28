package com.pepper.remote;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:基于spring-security的权限header provider
 */
public class AuthHttpHeaderProvider {

    private SecurityProperties properties;

    public AuthHttpHeaderProvider(SecurityProperties properties) {
        this.properties = properties;
    }

    public org.apache.http.Header getHeaders() {
        String username = properties.getUser().getName();
        String password = properties.getUser().getPassword();
        Header headers = new BasicHeader(HttpHeaders.AUTHORIZATION, encode(username, password));
        return headers;
    }

    protected String encode(String username, String password) {
        String token = Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }


}

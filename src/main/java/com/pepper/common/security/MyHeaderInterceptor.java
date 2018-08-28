package com.pepper.common.security;


import java.io.IOException;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:基于Basic认证的headder拦截器
 */
public class MyHeaderInterceptor implements ClientHttpRequestInterceptor {
    public static final String REQUEST_SOURCE_APP = "request-source-app";

    private SecurityProperties securityProperties;
    private final String requestSourceApp;

    public MyHeaderInterceptor(SecurityProperties securityProperties, String requestSourceApp) {
        this.securityProperties = securityProperties;
        this.requestSourceApp = requestSourceApp;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        // 增加请求来源header
        headers.add(REQUEST_SOURCE_APP, requestSourceApp);

        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            // 设置请求安全header
            RestAuthHttpHeaderProvider authProvider = new RestAuthHttpHeaderProvider(
                    securityProperties.getUser().getName(), securityProperties.getUser().getPassword());
            headers.add(authProvider.getHeaders().getName(), authProvider.getHeaders().getValue());
        }
        return execution.execute(request, body);
    }

}

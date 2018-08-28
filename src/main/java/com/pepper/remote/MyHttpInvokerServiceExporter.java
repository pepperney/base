package com.pepper.remote;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.util.StringUtils;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:重写HttpInvokerServiceExporter 增加从请求中获取来源系统header并放入线程上下文
 */
public class MyHttpInvokerServiceExporter extends HttpInvokerServiceExporter {

    public static final String REMOTING_REMOTEINVOCATION = "remoting-remoteInvocation";

    /**
     * Read a RemoteInvocation from the given HTTP request.
     * <p>
     * Delegates to
     * {@link #readRemoteInvocation(javax.servlet.http.HttpServletRequest, java.io.InputStream)}
     * with the {@link javax.servlet.ServletRequest#getInputStream() servlet
     * request's input stream}.
     *
     * @param request current HTTP request
     * @return the RemoteInvocation object
     * @throws IOException            in case of I/O failure
     * @throws ClassNotFoundException if thrown by deserialization
     */
    protected RemoteInvocation readRemoteInvocation(HttpServletRequest request) throws IOException, ClassNotFoundException {

        RemoteInvocation remoteInvocation = super.readRemoteInvocation(request, request.getInputStream());

        try {
            String system = request.getHeader(RemoteClientBuilder.REQUEST_SOURCE_APP);
            // 设置系统来源
            if (StringUtils.hasText(system)) {
                request.setAttribute(RemoteClientBuilder.REQUEST_SOURCE_APP, system);
                // 为空或则来源未知则取对方的host
            } else if (!StringUtils.hasText(system) || "unknown".equals(system)) {
                request.setAttribute(RemoteClientBuilder.REQUEST_SOURCE_APP, request.getRemoteHost());
            }
            // 设置请求参数
            request.setAttribute(REMOTING_REMOTEINVOCATION, remoteInvocation);
        } catch (Exception e) {
        }
        return remoteInvocation;
    }
}
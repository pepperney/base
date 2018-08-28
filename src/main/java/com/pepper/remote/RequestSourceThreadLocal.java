package com.pepper.remote;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:获取当前线程中的请求系统
 */
public class RequestSourceThreadLocal {
    private static ThreadLocal<String> REQUEST_SOURCE_APP_LOCAL = new ThreadLocal<String>();

    public static String getSystemSourceApp() {
        return REQUEST_SOURCE_APP_LOCAL.get();
    }

    public static void setSystemSourceApp(String requestSourceApp) {
        REQUEST_SOURCE_APP_LOCAL.set(requestSourceApp);
    }

}

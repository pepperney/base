package com.pepper.common.logback;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.util.CachingDateFormatter;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
public class JsonLayout  extends  LayoutBase<ILoggingEvent> {

    private String appName ;
    private String nodeName ;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    private CachingDateFormatter cachingDateFormatter = new CachingDateFormatter("yyyy-MM-dd HH:mm:ss.SSS");

    public static ObjectMapper mapper = newObjectMapper();
    private static ObjectMapper newObjectMapper() {
        ObjectMapper result = new ObjectMapper();

        result.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        result.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        result.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        result.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //屏蔽重复部分 result.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return result;
    }

    private StringBuilder sbuf = new StringBuilder(128);

    @Override
    public String doLayout(ILoggingEvent event) {
        if (!isStarted()) {
            return CoreConstants.EMPTY_STRING;
        }

        this.sbuf.setLength(0);
        StackTraceElement[] cda = event.getCallerData();

        String lineNumber = CallerData.NA;
        String methodName = CallerData.NA;
        String className  = CallerData.NA;
        String fileName = CallerData.NA ;
        String level = event.getLevel().toString();
        String threadName = event.getThreadName();
        String logTime = cachingDateFormatter.format(event.getTimeStamp());

        //处理message消息
        sbuf.append(event.getFormattedMessage());
        if(event.getThrowableProxy()!=null){
            sbuf.append(CoreConstants.LINE_SEPARATOR);
            sbuf.append(event.getThrowableProxy().getClassName()).append(":").append(event.getThrowableProxy().getMessage()).append(CoreConstants.LINE_SEPARATOR) ;
            StackTraceElementProxy[] stackTraceElementProxyArray = event.getThrowableProxy().getStackTraceElementProxyArray() ;
            for (StackTraceElementProxy step : stackTraceElementProxyArray) {
                String string = step.toString();
                sbuf.append(CoreConstants.TAB).append(string);
                ThrowableProxyUtil.subjoinPackagingData(sbuf, step);
                sbuf.append(CoreConstants.LINE_SEPARATOR);
            }
        }

        if (cda != null && cda.length > 0) {
            lineNumber =  Integer.toString(cda[0].getLineNumber());
            methodName = cda[0].getMethodName() ;
            className = cda[0].getClassName() ;
            fileName = cda[0].getFileName();
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("level", level);
        map.put("threadName", threadName);
        map.put("className", className);
        map.put("methodName", methodName);
        map.put("lineNumber", lineNumber);
        map.put("fileName", fileName);
        map.put("appName", appName);
        map.put("nodeName", nodeName);
        map.put("logTime", logTime);
        map.put("message", this.sbuf.toString());

        //清楚所有内容,用来存放输出json
        this.sbuf.setLength(0);

        try{
            this.sbuf.append(mapper.writeValueAsString(map)).append(CoreConstants.LINE_SEPARATOR);
        } catch(Exception e)
        {
            System.out.println("转换json异常" + map.toString());
            return CoreConstants.EMPTY_STRING;
        }

        return this.sbuf.toString();
    }

    @Override
    public void start() {
        setNodeName(getHostName(getInetAddress())+":"+getProcessID());
        super.start();
    }

    public static InetAddress getInetAddress(){

        try{
            return InetAddress.getLocalHost();
        }catch(UnknownHostException e){
            System.out.println("unknown host!");
        }
        return null;

    }

    public static String getHostIp(InetAddress netAddress){
        if(null == netAddress){
            return "";
        }
        String ip = netAddress.getHostAddress();
        return ip;
    }

    public static String getHostName(InetAddress netAddress){
        if(null == netAddress){
            return "";
        }
        String name = netAddress.getHostName();
        return name;
    }

    public static final String getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getName().split("@")[0];
    }

}


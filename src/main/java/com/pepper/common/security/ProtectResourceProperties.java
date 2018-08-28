package com.pepper.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/25
 * @Description:系统间访问受保护资源属性
 */
@ConfigurationProperties(prefix = ProtectResourceProperties.PREX)
public class ProtectResourceProperties {
    public static final String PREX = "protected.resources";

    /**
     * 需要保护的资源路径格式 /xxx,/yyy
     */
    private List<String> url = new ArrayList<String>();

    /**
     * 白名单IP
     */
    private List<String> whiteIp = new ArrayList<String>();

    /**
     * 白名单系统
     */
    private List<String> whiteDomain = new ArrayList<String>();

    public List<String> getUrl() {
        return url;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }

    public List<String> getWhiteIp() {
        return whiteIp;
    }

    public void setWhiteIp(List<String> whiteIp) {
        this.whiteIp = whiteIp;
    }

    public List<String> getWhiteDomain() {
        return whiteDomain;
    }

    public void setWhiteDomain(List<String> whiteDomain) {
        this.whiteDomain = whiteDomain;
    }

}

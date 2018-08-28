package com.pepper.common.logback;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
public class JsonEncoder extends LayoutWrappingEncoder<ILoggingEvent>{

    private String appName = "default-app";
    private String nodeName = "default-node";

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

    @Override
    public void start() {
        JsonLayout layout = new JsonLayout();
        layout.setContext(context);
        layout.setAppName(getAppName());
        layout.setNodeName(getNodeName());
        layout.start();
        this.layout = layout;
        super.start();
    }

}

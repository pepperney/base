package com.pepper.remote;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import lombok.extern.slf4j.Slf4j;
/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description: HTTP Request拦截器,对使用熔断组件Hystrix与灰度功能的整合,对Hystrix的独立线程池进行操作
 */
@Slf4j
public class GatedHeaderInterceptor extends HandlerInterceptorAdapter {
    public static final String HEADER_LABEL = "x-label";
    public static final String HEADER_LABEL_SPLIT = ",";

    public static final HystrixRequestVariableDefault<List<String>> label =
            new HystrixRequestVariableDefault<>();

    /**
     * 初始化Hystrix线程池
     *
     * @param labels
     */
    public static void initHystrixRequestContext(String labels) {
        log.info("label: " + labels);
        if (!HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.initializeContext();
        }

        /**
         * 将值设置到变量里
         */
        if (!StringUtils.isEmpty(labels)) {
            GatedHeaderInterceptor.label
                    .set(Arrays.asList(labels.split(GatedHeaderInterceptor.HEADER_LABEL_SPLIT)));
        } else {
            GatedHeaderInterceptor.label.set(Collections.emptyList());
        }
    }

    /**
     * 关闭线程池
     */
    public static void shutdownHystrixRequestContext() {
        if (HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.getContextForCurrentThread().shutdown();
        }
    }

    /**
     * 预处理,从HTTP请求头拿到labels,并放入Hystrix线程变量
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        GatedHeaderInterceptor.initHystrixRequestContext(request.getHeader(GatedHeaderInterceptor.HEADER_LABEL));
        return true;
    }

    /**
     * 后处理,关闭线程池
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        GatedHeaderInterceptor.shutdownHystrixRequestContext();
    }

}

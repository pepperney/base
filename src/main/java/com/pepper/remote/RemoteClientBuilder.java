package com.pepper.remote;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.pepper.remote.annotation.RemoteType;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.httpinvoker.*;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
public class RemoteClientBuilder implements EnvironmentAware {
    private static final Logger log = LoggerFactory.getLogger(RemoteClientBuilder.class);

    private Map<String, Object> clients = new ConcurrentHashMap<>();

    private Environment env;

    private RemoteClientProperties properties;

    private HttpInvokerRequestExecutor httpInvokerRequestExecutor;

    private HttpInvokerRequestExecutor ribbonHttpInvokerRequestExecutor;

    private LoadBalancerClient loadBalancerClient;

    public static final String REQUEST_SOURCE_APP = "request-source-app";
    private SecurityProperties securityProperties;

    public RemoteClientBuilder(SecurityProperties securityProperties, RemoteClientProperties properties) {
        this(securityProperties, properties, null);
    }

    public RemoteClientBuilder(SecurityProperties securityProperties, RemoteClientProperties properties, LoadBalancerClient loadBalancerClient) {
        this.properties = properties;
        this.loadBalancerClient = loadBalancerClient;
        this.securityProperties = securityProperties;
        this.initHttpInvokerRequestExecutor();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    public <T> T build(Class<T> serviceInterface, String url) {
        return build(serviceInterface, url, RemoteType.HTTP, null, false);
    }

    public <T> T build(Class<T> serviceInterface, String url, boolean loadBalanced) {
        return build(serviceInterface, url, RemoteType.HTTP, null, loadBalanced);
    }

    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> serviceInterface, String url, RemoteType type, String name, boolean loadBalanced) {
        Object value = null;

        synchronized (clients) {
            String key = serviceInterface.getName();

            // 从缓存中获取 @RemoteClient 的实例
            value = clients.get(key);
            if (value == null) {
                if (StringUtils.isEmpty(name)) {
                    name = serviceInterface.getSimpleName();
                }

                url = resolverUrl(url);
                if (null == url) {
                    throw new IllegalArgumentException("remote call url is null , please check your configration!");
                }
                if (!url.endsWith(name)) {
                    if (url.endsWith("/")) {
                        url += name;
                    } else {
                        url += ("/" + name);
                    }
                }

                // 为了兼容，从配置文件里判断是否为注册服务
                if (!loadBalanced) {
                    String serviceName = URI.create(url).getHost();
                    if (null != serviceName) {
                        serviceName = serviceName.toUpperCase();
                    }
                    List<String> resultList = new ArrayList<String>();
                    if (null != this.properties.getLoadBalancedServices()) {
                        for (String s : this.properties.getLoadBalancedServices()) {
                            if (null != s) {
                                resultList.add(s.toUpperCase());
                            }
                        }
                    }
                    loadBalanced = resultList.contains(serviceName);
                }

                if (loadBalanced && RemoteType.HTTP != type) {
                    throw new UnsupportedOperationException("When RemoteClient 'loadBalanced' is true, 'type' only support: HTTP.");
                }

                if (RemoteType.HTTP == type) {
                    HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
                    factory.setServiceUrl(url);
                    factory.setServiceInterface(serviceInterface);
                    factory.setHttpInvokerRequestExecutor(loadBalanced ? this.ribbonHttpInvokerRequestExecutor : this.httpInvokerRequestExecutor);
                    factory.afterPropertiesSet();

                    value = factory.getObject();
                } else if (RemoteType.HESSIAN == type) {
                    HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
                    factory.setServiceUrl(url);
                    factory.setServiceInterface(serviceInterface);
                    factory.afterPropertiesSet();

                    value = factory.getObject();
                } else if (RemoteType.RMI == type) {
                    RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
                    factory.setServiceUrl(url);
                    factory.setServiceInterface(serviceInterface);
                    factory.afterPropertiesSet();

                    value = factory.getObject();
                }

                // 缓存 @RemoteClient 的实例
                clients.put(key, value);
            } else {
                log.debug("Build remote client class:{} from cache ", serviceInterface.getName());
            }
        }

        return (T) value;
    }

    protected void initHttpInvokerRequestExecutor() {
        if (properties.getMaxTotalConn() > 0) {
            HttpClient httpClient = createDefaultHttpClient(properties.getMaxTotalConn(), properties.getMaxConnPerRoute());

            HttpComponentsHttpInvokerRequestExecutor executor = new EyasRibbonHttpComponentsHttpInvokerRequestExecutor(httpClient);
            executor.setConnectTimeout(properties.getConnectTimeout());
            executor.setReadTimeout(properties.getReadTimeout());
            this.httpInvokerRequestExecutor = executor;

            if (this.loadBalancerClient != null) {
                executor = new RibbonHttpComponentsHttpInvokerRequestExecutor(loadBalancerClient, httpClient);
                executor.setConnectTimeout(properties.getConnectTimeout());
                executor.setReadTimeout(properties.getReadTimeout());
                this.ribbonHttpInvokerRequestExecutor = executor;
            }
        } else {
            SimpleHttpInvokerRequestExecutor executor = new EyasSimpleHttpInvokerRequestExecutor();

            executor.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
            executor.setConnectTimeout(properties.getConnectTimeout());
            executor.setReadTimeout(properties.getReadTimeout());

            this.httpInvokerRequestExecutor = executor;

            if (this.loadBalancerClient != null) {
                executor = new RibbonSimpleHttpInvokerRequestExecutor(this.loadBalancerClient);

                executor.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
                executor.setConnectTimeout(properties.getConnectTimeout());
                executor.setReadTimeout(properties.getReadTimeout());

                this.ribbonHttpInvokerRequestExecutor = executor;
            }
        }
    }

    private HttpClient createDefaultHttpClient(int maxTotalConn, int maxConnPerRoute) {
        Registry<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(maxTotalConn);
        connectionManager.setDefaultMaxPerRoute(maxConnPerRoute);

        return HttpClientBuilder.create().setConnectionManager(connectionManager).evictExpiredConnections()
                .evictIdleConnections(properties.getMaxIdleTime(), TimeUnit.MILLISECONDS).build();
    }

    private String resolverUrl(String url) {
        url = url.trim();

        if (url.startsWith("${") && url.endsWith("}")) {
            url = url.substring(2, url.length() - 1);

            url = env.getProperty(url);
        }

        return url;
    }

    /**
     * 重写HttpComponentsHttpInvokerRequestExecutor 添加请求来源header
     *
     * @author
     */
    class EyasRibbonHttpComponentsHttpInvokerRequestExecutor extends HttpComponentsHttpInvokerRequestExecutor {
        public EyasRibbonHttpComponentsHttpInvokerRequestExecutor(HttpClient httpClient) {
            super(httpClient);
        }

        @Override
        protected HttpResponse executeHttpPost(HttpInvokerClientConfiguration config, HttpClient httpClient, HttpPost httpPost) throws IOException {
            // 增加请求来源header
            httpPost.addHeader(REQUEST_SOURCE_APP, getRequestSourceApp());
            // 设置请求安全header
            AuthHttpHeaderProvider authProvider = new AuthHttpHeaderProvider(securityProperties);
            httpPost.addHeader(authProvider.getHeaders());
            // 设置灰度标识
            if (HystrixRequestContext.isCurrentThreadInitialized()) {
                // 从线程变量中获取label
                String header = StringUtils.collectionToDelimitedString(GatedHeaderInterceptor.label.get(), GatedHeaderInterceptor.HEADER_LABEL_SPLIT);
                log.debug("gray label header: {}", header);
                httpPost.addHeader(GatedHeaderInterceptor.HEADER_LABEL, header);
            }
            return super.executeHttpPost(config, httpClient, httpPost);
        }
    }

    class RibbonHttpComponentsHttpInvokerRequestExecutor extends HttpComponentsHttpInvokerRequestExecutor {
        private LoadBalancerClient loadBalancerClient;

        public RibbonHttpComponentsHttpInvokerRequestExecutor(LoadBalancerClient loadBalancerClient, HttpClient httpClient) {
            super(httpClient);
            this.loadBalancerClient = loadBalancerClient;
        }

        @Override
        protected RemoteInvocationResult doExecuteRequest(final HttpInvokerClientConfiguration config, final ByteArrayOutputStream baos)
                throws IOException, ClassNotFoundException {
            final URI serviceUri = URI.create(config.getServiceUrl());

            String serviceName = serviceUri.getHost();
            return this.loadBalancerClient.execute(serviceName, new LoadBalancerRequest<RemoteInvocationResult>() {
                @Override
                public RemoteInvocationResult apply(ServiceInstance instance) throws Exception {
                    HttpPost postMethod = createHttpPost(config);
                    // 增加请求来源header
                    postMethod.addHeader(REQUEST_SOURCE_APP, getRequestSourceApp());
                    // 设置请求安全header
                    AuthHttpHeaderProvider authProvider = new AuthHttpHeaderProvider(securityProperties);
                    // 设置灰度标识
                    if (HystrixRequestContext.isCurrentThreadInitialized()) {
                        // 从线程变量中获取label
                        String header = StringUtils.collectionToDelimitedString(GatedHeaderInterceptor.label.get(), GatedHeaderInterceptor.HEADER_LABEL_SPLIT);
                        log.debug("gray label header: {}", header);
                        postMethod.addHeader(GatedHeaderInterceptor.HEADER_LABEL, header);
                    }

                    postMethod.addHeader(authProvider.getHeaders());
                    postMethod.setURI(loadBalancerClient.reconstructURI(instance, serviceUri));
                    setRequestBody(config, postMethod, baos);

                    try {
                        HttpResponse response = executeHttpPost(config, getHttpClient(), postMethod);
                        validateResponse(config, response);
                        InputStream responseBody = getResponseBody(config, response);

                        return readRemoteInvocationResult(responseBody, config.getCodebaseUrl());
                    } finally {
                        postMethod.releaseConnection();
                    }
                }
            });
        }
    }

    /**
     * 获取当前客户端系统域名
     *
     * @return
     */
    private String getRequestSourceApp() {
        try {
            String system = env.getProperty("spring.application.name");
            if (!StringUtils.hasText(system)) {
                String logPath = env.getProperty("logging.path");
                if (StringUtils.hasText(logPath)) {
                    int index = logPath.lastIndexOf("/");
                    return logPath.substring(index + 1, logPath.length());
                }
            } else {
                return new StringBuffer(system).append(".pepper.com").toString();
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    class EyasSimpleHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {

        protected HttpURLConnection openConnection(HttpInvokerClientConfiguration config) throws IOException {
            URLConnection con = new URL(config.getServiceUrl()).openConnection();
            if (!(con instanceof HttpURLConnection)) {
                throw new IOException("Service URL [" + config.getServiceUrl() + "] is not an HTTP URL");
            }

            AuthHttpHeaderProvider authProvider = new AuthHttpHeaderProvider(securityProperties);
            // 设置请求来源header
            con.setRequestProperty(REQUEST_SOURCE_APP, getRequestSourceApp());
            // 设置请求安全header
            con.setRequestProperty(authProvider.getHeaders().getName(), authProvider.getHeaders().getValue());
            // 设置灰度header
            if (HystrixRequestContext.isCurrentThreadInitialized()) {
                // 从线程变量中获取label
                String header = StringUtils.collectionToDelimitedString(GatedHeaderInterceptor.label.get(),
                        GatedHeaderInterceptor.HEADER_LABEL_SPLIT);
                log.debug("gray label header: {}", header);
                con.setRequestProperty(GatedHeaderInterceptor.HEADER_LABEL, header);
            }
            return (HttpURLConnection) con;
        }
    }

    class RibbonSimpleHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {
        private LoadBalancerClient loadBalancerClient;

        public RibbonSimpleHttpInvokerRequestExecutor(LoadBalancerClient loadBalancerClient) {
            this.loadBalancerClient = loadBalancerClient;
        }

        protected HttpURLConnection openConnection(HttpInvokerClientConfiguration config) throws IOException {
            URLConnection con = new URL(config.getServiceUrl()).openConnection();
            if (!(con instanceof HttpURLConnection)) {
                throw new IOException("Service URL [" + config.getServiceUrl() + "] is not an HTTP URL");
            }

            AuthHttpHeaderProvider authProvider = new AuthHttpHeaderProvider(securityProperties);
            // 设置请求来源header
            con.setRequestProperty(RemoteClientBuilder.REQUEST_SOURCE_APP, getRequestSourceApp());
            // 设置请求安全header
            con.setRequestProperty(authProvider.getHeaders().getName(), authProvider.getHeaders().getValue());
            // 设置灰度header
            if (HystrixRequestContext.isCurrentThreadInitialized()) {
                // 从线程变量中获取label
                String header = StringUtils.collectionToDelimitedString(GatedHeaderInterceptor.label.get(), GatedHeaderInterceptor.HEADER_LABEL_SPLIT);
                log.debug("gray label header: {}", header);
                con.setRequestProperty(GatedHeaderInterceptor.HEADER_LABEL, header);
            }
            return (HttpURLConnection) con;
        }

        @Override
        protected RemoteInvocationResult doExecuteRequest(final HttpInvokerClientConfiguration config, final ByteArrayOutputStream baos)
                throws IOException, ClassNotFoundException {

            final URI serviceUri = URI.create(config.getServiceUrl());

            String serviceName = serviceUri.getHost();
            return this.loadBalancerClient.execute(serviceName, new LoadBalancerRequest<RemoteInvocationResult>() {
                @Override
                public RemoteInvocationResult apply(final ServiceInstance instance) throws Exception {

                    URI uri = loadBalancerClient.reconstructURI(instance, serviceUri);
                    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();

                    AuthHttpHeaderProvider authProvider = new AuthHttpHeaderProvider(securityProperties);
                    // 设置请求来源header
                    con.setRequestProperty(REQUEST_SOURCE_APP, getRequestSourceApp());
                    // 设置请求安全header
                    con.setRequestProperty(authProvider.getHeaders().getName(), authProvider.getHeaders().getValue());
                    // 设置灰度header
                    if (HystrixRequestContext.isCurrentThreadInitialized()) {
                        // 从线程变量中获取label
                        String header = StringUtils.collectionToDelimitedString(GatedHeaderInterceptor.label.get(),
                                GatedHeaderInterceptor.HEADER_LABEL_SPLIT);
                        log.debug("gray label header: {}", header);
                        con.setRequestProperty(GatedHeaderInterceptor.HEADER_LABEL, header);
                    }
                    prepareConnection(con, baos.size());
                    writeRequestBody(config, con, baos);
                    validateResponse(config, con);
                    InputStream responseBody = readResponseBody(config, con);

                    return readRemoteInvocationResult(responseBody, config.getCodebaseUrl());
                }
            });
        }
    }
}

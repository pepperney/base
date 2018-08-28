package com.pepper.remote;

import com.pepper.remote.annotation.RemoteClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
public class RemoteClientBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
    private RemoteClientBuilder clientBuilder;

    public RemoteClientBeanPostProcessor(RemoteClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            RemoteClient client = field.getAnnotation(RemoteClient.class);
            if (client != null) {
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new IllegalStateException("@RemoteClient annotation is not supported on static fields ");
                }

                String url = StringUtils.isEmpty(client.value()) ? client.url() : client.value();

                // 生成 @RemoteClient 的实例
                Object value = clientBuilder.build(field.getType(), url, client.type(), client.name(), client.loadBalanced());

                // 使变量域可用
                ReflectionUtils.makeAccessible(field);
                try {
                    field.set(bean, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + e);
                }
            }
        }

        return true;
    }
}


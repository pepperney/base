package com.pepper.remote;

import com.pepper.remote.annotation.RemoteService;
import com.pepper.remote.annotation.RemoteServiceScan;
import com.pepper.remote.annotation.RemoteType;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;


/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
public class RemoteServiceScannerRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RemoteServiceScan.class.getName()));

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        scanner.addIncludeFilter(new AnnotationTypeFilter(RemoteService.class));
        scanner.doScan(annoAttrs.getStringArray("value"));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private static class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {
        public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
            super(registry);
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

            if (beanDefinitions.isEmpty()) {
                logger.warn("No Remote Service was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
            } else {
                processBeanDefinitions(beanDefinitions);
            }

            return beanDefinitions;
        }

        private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
            for (BeanDefinitionHolder holder : beanDefinitions) {
                try {
                    MetadataReader mr = this.getMetadataReaderFactory().getMetadataReader(holder.getBeanDefinition().getBeanClassName());
                    AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(mr.getAnnotationMetadata().getAnnotationAttributes(RemoteService.class.getName()));

                    if (null == annoAttrs) {
                        continue;
                    }
                    Class<?> serviceInterface = annoAttrs.getClass("value");
                    if (serviceInterface == null) {
                        serviceInterface = annoAttrs.getClass("serviceInterface");
                    }

                    String name = annoAttrs.getString("name");
                    if (StringUtils.isEmpty(name)) {
                        name = serviceInterface.getSimpleName();
                    }

                    String path = annoAttrs.getString("path");
                    if (!StringUtils.isEmpty(path)) {
                        name = path + "/" + name;
                    }

                    if (!name.startsWith("/")) {
                        name = "/" + name;
                    }

                    GenericBeanDefinition definition = new GenericBeanDefinition();
                    definition.getPropertyValues().add("service", new RuntimeBeanReference(holder.getBeanName()));
                    definition.getPropertyValues().add("serviceInterface", serviceInterface);
                    definition.setInitMethodName("afterPropertiesSet");

                    RemoteType type = annoAttrs.getEnum("type");
                    if (RemoteType.HTTP == type) {
                        definition.setBeanClass(MyHttpInvokerServiceExporter.class);
                    } else if (RemoteType.HESSIAN == type) {
                        definition.setBeanClass(HessianServiceExporter.class);
                    } else if (RemoteType.RMI == type) {
                        definition.setBeanClass(RmiServiceExporter.class);
                    }

                    registerBeanDefinition(new BeanDefinitionHolder(definition, name), this.getRegistry());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new BeanDefinitionStoreException(
                            "Failed to read candidate component class: " + holder.getBeanDefinition().getBeanClassName(), e);
                }
            }
        }
    }
}

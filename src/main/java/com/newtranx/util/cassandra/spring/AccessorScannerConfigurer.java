/*
 * Copyright 2016 NewTranx Co. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.newtranx.util.cassandra.spring;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Accessor;
import com.newtranx.util.Lazy;

/**
 * 自动扫描Accessor接口，生成实现（代理）类并注册到Spring context中
 * 
 * @author luyi
 *
 */
public class AccessorScannerConfigurer implements BeanFactoryPostProcessor, ApplicationContextAware {

	private static Logger log = LogManager.getLogger(AccessorScannerConfigurer.class);

	private String basePackage;

	private ApplicationContext mainContext;

	private Session session;

	public void setSession(Session session) {
		System.err.println(session);
		this.session = session;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.mainContext = context;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory context) throws BeansException {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {

			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return beanDefinition.getMetadata().isInterface();
			}

		};
		scanner.addIncludeFilter(new AnnotationTypeFilter(Accessor.class));
		for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
			Class<?> accessorCls;
			try {
				accessorCls = Class.forName(bd.getBeanClassName());
			} catch (ClassNotFoundException e) {
				throw new AssertionError(e);
			}
			log.info("Creating proxy accessor: " + accessorCls.getName());
			MethodInterceptor interceptor = new MethodInterceptor() {

				private final Lazy<?> target = new Lazy<>(() -> {
					log.info("Creating actual accessor: " + accessorCls.getName());
					Session session;
					if (AccessorScannerConfigurer.this.session == null)
						session = mainContext.getBean(Session.class);
					else
						session = AccessorScannerConfigurer.this.session;
					MappingManager mappingManager = new MappingManager(session);
					return mappingManager.createAccessor(accessorCls);
				});

				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					if ("toString".equals(method.getName())) {
						return accessorCls.getName();
					}
					return method.invoke(target.get(), args);
				}

			};
			Enhancer enhancer = new Enhancer();
			enhancer.setInterfaces(new Class<?>[] { accessorCls });
			enhancer.setCallback(interceptor);
			Object bean = enhancer.create();
			String beanName = StringUtils.uncapitalize(accessorCls.getSimpleName());
			context.registerSingleton(beanName, bean);
			log.info("Bean registed, name=" + beanName + ", bean=" + bean.toString());
		}
	}

}

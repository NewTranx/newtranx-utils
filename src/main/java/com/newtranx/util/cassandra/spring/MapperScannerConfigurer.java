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
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objenesis.ObjenesisHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Table;
import com.newtranx.util.Lazy;

import rx.exceptions.Exceptions;

/**
 * 自动扫描Cassandra实体类并注册对应的Mapper到Spring context中
 * 
 * @author luyi
 *
 */
public class MapperScannerConfigurer implements BeanFactoryPostProcessor, ApplicationContextAware {

	private static Logger log = LogManager.getLogger(MapperScannerConfigurer.class);

	private static final Object lock = new Object();

	private String basePackage;

	private Session session;

	private ApplicationContext mainContext;

	public void setSession(Session session) {
		this.session = session;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.mainContext = context;
	}

	@SuppressWarnings("unused") // compiler bug?
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory context) throws BeansException {
		synchronized (lock) {
			ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
					false);
			scanner.addIncludeFilter(new AnnotationTypeFilter(Table.class));
			for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
				Class<?> entityCls;
				try {
					entityCls = Class.forName(bd.getBeanClassName());
				} catch (ClassNotFoundException e) {
					throw new AssertionError(e);
				}
				log.info("Creating proxy mapper for entity: " + entityCls.getName());
				CassandraMapper annotation = entityCls.getAnnotation(CassandraMapper.class);
				Mapper<?> bean = createProxy(Mapper.class, new MyInterceptor(entityCls, annotation.singleton()));
				String beanName;
				if (annotation == null)
					beanName = StringUtils.uncapitalize(entityCls.getSimpleName()) + "Mapper";
				else
					beanName = annotation.value();
				context.registerSingleton(beanName, bean);
				log.info("Bean registed, name=" + beanName + ", bean=" + bean.toString());
			}
		}
	}

	private class MyInterceptor implements MethodInterceptor {

		private final Class<?> entityCls;

		private final Lazy<Mapper<?>> singletonTarget;

		private final Supplier<Mapper<?>> targetInitializer;

		private final boolean singleton;

		MyInterceptor(Class<?> entityCls, boolean singleton) {
			super();
			this.entityCls = entityCls;
			this.targetInitializer = () -> {
				if (singleton)
					log.info("Creating actual mapper for entity: " + entityCls.getName());
				Session session;
				if (MapperScannerConfigurer.this.session == null)
					session = mainContext.getBean(Session.class);
				else
					session = MapperScannerConfigurer.this.session;
				MappingManager mappingManager = new MappingManager(session);
				return mappingManager.mapper(entityCls);
			};
			this.singleton = singleton;
			if (singleton)
				this.singletonTarget = new Lazy<>(targetInitializer);
			else
				this.singletonTarget = null;
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if ("toString".equals(method.getName())) {
				return "Mapper<" + entityCls + ">";
			}
			return method.invoke(getTarget(), args);
		}

		private Mapper<?> getTarget() {
			if (singleton)
				try {
					return singletonTarget.get();
				} catch (ConcurrentException e) {
					throw Exceptions.propagate(e);
				}
			else
				return targetInitializer.get();
		}

	}

	@SuppressWarnings("unchecked")
	private static <T> T createProxy(final Class<?> classToMock, final MethodInterceptor interceptor) {
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(classToMock);
		enhancer.setCallbackType(interceptor.getClass());
		final Class<?> proxyClass = enhancer.createClass();
		Enhancer.registerCallbacks(proxyClass, new Callback[] { interceptor });
		return (T) ObjenesisHelper.newInstance(proxyClass);
	}

}

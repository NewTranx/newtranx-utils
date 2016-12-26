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

package com.newtranx.util.mysql.fabric;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mysql.fabric.FabricCommunicationException;
import com.mysql.fabric.proto.xmlrpc.XmlRpcClient;
import com.newtranx.util.aop.AspectJUtils;

import rx.exceptions.Exceptions;

@Component
@Aspect
public class SpringQueryAllShardsAspect {

	private static final Logger log = LogManager.getLogger(SpringQueryAllShardsAspect.class);

	private static Class<?>[] EMPTY_PARAM = new Class<?>[0];

	private static Object[] EMPTY_ARGS = new Object[0];

	@Autowired
	private FabricMySQLDataSourceEx ds;

	@Autowired
	private XmlRpcClient fabricClient;

	@Autowired
	private ApplicationContext applicationContext;

	private LoadingCache<Object, Set<String>> groupsCache;

	private Object cacheKey = new Object();

	@Value("${mysql.fabric.query_all_shards.groups.cache.expire.seconds:600}")
	private int groupsCacheExpireTime;

	@PostConstruct
	public void initGroupsCache() {
		groupsCache = CacheBuilder.newBuilder().expireAfterWrite(groupsCacheExpireTime, TimeUnit.SECONDS)
				.build(new CacheLoader<Object, Set<String>>() {
					public Set<String> load(Object key) throws FabricCommunicationException {
						synchronized (fabricClient) {
							log.info("Loading groups");
							return fabricClient.getGroupNames();
						}
					}
				});
	}

	@Around("@annotation(com.newtranx.util.mysql.fabric.QueryAllShards)")
	public Object union(ProceedingJoinPoint pjp) throws Throwable {
		Method method = AspectJUtils.getMethod(pjp);
		QueryAllShards annotation = method.getAnnotation(QueryAllShards.class);
		String table = annotation.table();
		log.debug("Table=" + table);
		Set<String> groups = groupsCache.get(cacheKey);
		log.debug("ServerGroups=" + groups);
		List<Object> list;
		boolean readOnly = annotation.readOnly();
		Pattern excludePattern;
		String excludeRegex = annotation.excludeShardsPatternRegex();
		if (!StringUtils.isEmpty(excludeRegex)) {
			excludePattern = Pattern.compile(excludeRegex);
		} else {
			excludePattern = null;
		}

		Function<Boolean, List<Object>> computeFunction = (par) -> {
			Stream<String> stream = groups.stream();
			if (par)
				stream = stream.parallel();
			return stream.filter(gp -> {
				boolean exclude = excludePattern != null && excludePattern.matcher(gp).matches();
				if (exclude) {
					log.debug("Skipping group:" + gp);
				}
				return !exclude;
			}).map(gp -> {
				log.debug("Querying group: " + gp);
				ds.whenNewConnection().doInit(conn -> conn.setServerGroupName(gp))
						.doInit(conn -> conn.setReadOnly(readOnly));
				try {
					return pjp.proceed();
				} catch (Throwable t) {
					throw Exceptions.propagate(t);
				} finally {
					ds.clearInitOps();
				}
			}).collect(Collectors.toList());
		};

		if (StringUtils.isEmpty(annotation.parallelPool())) {
			list = computeFunction.apply(false);
		} else {
			ForkJoinPool pool;
			if ("!jdkCommon".equals(annotation.parallelPool()))
				pool = ForkJoinPool.commonPool();
			else
				pool = applicationContext.getBean(annotation.parallelPool(), ForkJoinPool.class);
			log.debug("Executing queries in parallel, pool=" + pool);
			list = pool.submit(() -> {
				return computeFunction.apply(true);
			}).get();
		}
		Aggregator aggregator;
		try {
			aggregator = (Aggregator) annotation.aggregator().getDeclaredMethod("getInstance", EMPTY_PARAM).invoke(null,
					EMPTY_ARGS);
		} catch (Exception e) {
			log.warn(
					"Can not get singleton for class " + annotation.aggregator().getName() + ", creating new instance");
			aggregator = annotation.aggregator().newInstance();
		}
		return aggregator.apply(list);
	}

}

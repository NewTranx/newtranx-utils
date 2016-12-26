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
import java.lang.reflect.Parameter;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.newtranx.util.aop.AspectJUtils;

@Component
@Aspect
public class SpringMybatisSetShardKeyAspect {

	@Autowired
	private SpringMybatisSetShardKeyUtil fabricShardKey;

	@Around("@annotation(com.newtranx.util.mysql.fabric.WithShardKey) || @within(com.newtranx.util.mysql.fabric.WithShardKey)")
	public Object setShardKey(ProceedingJoinPoint pjp) throws Throwable {
		Method method = AspectJUtils.getMethod(pjp);
		String key = null;
		boolean force = method.getAnnotation(WithShardKey.class).force();
		int i = 0;
		for (Parameter p : method.getParameters()) {
			ShardKey a = p.getAnnotation(ShardKey.class);
			if (a != null) {
				if (key != null)
					throw new RuntimeException("found multiple shardkey");
				Object obj = pjp.getArgs()[i];
				if (StringUtils.isEmpty(a.property()))
					key = obj.toString();
				else {
					BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
					key = bw.getPropertyValue(a.property()).toString();
				}
			}
			i++;
		}
		if (key == null)
			throw new RuntimeException("can not find shardkey");
		fabricShardKey.set(key, force);
		return pjp.proceed();
	}

}

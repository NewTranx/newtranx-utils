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

package com.newtranx.util.monitoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.newtranx.util.monitoring.async.AsyncHandler;
import com.newtranx.util.monitoring.reporter.Reporter;

@Aspect
public class MonitorAspect {

	private List<Reporter> reporters = new ArrayList<>();

	private List<AsyncHandler> asyncHandlers = new ArrayList<>();

	@Around("@annotation(com.newtranx.util.monitoring.Monitoring)")
	public Object around(final ProceedingJoinPoint pjp) throws Throwable {
		final MethodSignature signature = (MethodSignature) pjp.getSignature();
		final Method method = getMethod(signature, pjp);
		final long begin = System.currentTimeMillis();
		MonitorContext monitorContext = new MonitorContext() {

			@Override
			public void doReport() {
				long end = System.currentTimeMillis();
				String name = method.getAnnotation(Monitoring.class).value().trim();
				Optional<String> optName = StringUtils.isEmpty(name) ? Optional.empty() : Optional.of(name);
				for (Reporter r : reporters) {
					try {
						r.report(method, end - begin, optName);
					} catch (Throwable t) {
						t.printStackTrace(System.err);
					}
				}
			}

		};
		Object[] args = pjp.getArgs();
		AsyncHandler asyncHandler = null;
		for (AsyncHandler a : getAsyncHandlers()) {
			Object[] processedArgs = a.preProcess(method, args, monitorContext);
			if (monitorContext.isAsync()) {
				args = processedArgs;
				asyncHandler = a;
				break;
			}
		}
		Object result = pjp.proceed(args);
		if (monitorContext.isAsync()) {
			return asyncHandler.postProcess(result, monitorContext);
		} else {
			monitorContext.doReport();
			return result;
		}
	}

	public List<AsyncHandler> getAsyncHandlers() {
		return asyncHandlers;
	}

	public void setAsyncHandlers(List<AsyncHandler> asyncHandlers) {
		this.asyncHandlers = asyncHandlers;
	}

	public List<Reporter> getReporters() {
		return reporters;
	}

	public void setReporters(List<Reporter> reporters) {
		this.reporters = reporters;
	}

	private static Method getMethod(MethodSignature signature, ProceedingJoinPoint pjp) {
		Method method = signature.getMethod();
		if (method.getDeclaringClass().isInterface()) {
			try {
				method = pjp.getTarget().getClass().getDeclaredMethod(pjp.getSignature().getName(),
						method.getParameterTypes());
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return method;
	}

}

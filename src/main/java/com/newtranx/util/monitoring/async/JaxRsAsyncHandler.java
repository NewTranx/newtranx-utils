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

package com.newtranx.util.monitoring.async;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.Suspended;

import com.newtranx.util.monitoring.MonitorContext;

public class JaxRsAsyncHandler implements AsyncHandler {

	private static final ConcurrentMap<Method, Integer> cache = new ConcurrentHashMap<>();

	private static final int IGNORE = Integer.MIN_VALUE;

	private static final int INIT = -1;

	@Override
	public Object[] preProcess(Method method, Object[] args, MonitorContext context) {
		int i = INIT;
		AsyncResponse asyncResponse = null;
		if (cache.containsKey(method)) {
			i = cache.get(method);
			if (i == IGNORE)
				return args;
			asyncResponse = (AsyncResponse) args[i];
		} else {
			for (Annotation a : method.getDeclaredAnnotations()) {
				if (a.annotationType().isAnnotationPresent(HttpMethod.class)) {
					Parameter[] params = method.getParameters();
					for (i = 0; i < args.length; i++) {
						Object arg = args[i];
						Parameter param = params[i];
						if (arg instanceof AsyncResponse && param.isAnnotationPresent(Suspended.class)) {
							cache.putIfAbsent(method, i);
							asyncResponse = (AsyncResponse) arg;
							break;
						}
					}
					break;
				}
			}
			if (asyncResponse == null)
				cache.putIfAbsent(method, IGNORE);
		}
		if (asyncResponse != null) {
			context.setAsync(true);
			asyncResponse.register(new CompletionCallback() {

				@Override
				public void onComplete(Throwable throwable) {
					context.doReport();
				}

			});
		}
		return args;
	}

}

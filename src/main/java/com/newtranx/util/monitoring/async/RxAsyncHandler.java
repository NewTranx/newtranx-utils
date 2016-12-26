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

import java.lang.reflect.Method;

import com.newtranx.util.monitoring.MonitorContext;

import rx.Observable;

public class RxAsyncHandler implements AsyncHandler {

	@Override
	public Object[] preProcess(Method method, Object[] args, MonitorContext context) {
		context.setAsync(Observable.class.isAssignableFrom(method.getReturnType()));
		return args;
	}

	@Override
	public Object postProcess(Object result, MonitorContext context) {
		if (result instanceof Observable<?>) {
			Observable<?> observable = (Observable<?>) result;
			return observable.doOnCompleted(context::doReport);
		} else {
			throw new IllegalArgumentException();
		}
	}

}

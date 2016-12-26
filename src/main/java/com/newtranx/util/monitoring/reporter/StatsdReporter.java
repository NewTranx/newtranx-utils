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

package com.newtranx.util.monitoring.reporter;

import java.lang.reflect.Method;
import java.util.Optional;

import com.timgroup.statsd.StatsDClient;

public class StatsdReporter implements Reporter {

	private StatsDClient client;

	@Override
	public void report(Method method, long respTime, Optional<String> optName) {
		String name = optName.orElse(method.getDeclaringClass().getSimpleName() + "." + method.getName());
		client.time(name, respTime);
	}

	public StatsDClient getClient() {
		return client;
	}

	public void setClient(StatsDClient client) {
		this.client = client;
	}

}

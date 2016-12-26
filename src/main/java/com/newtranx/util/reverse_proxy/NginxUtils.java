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

package com.newtranx.util.reverse_proxy;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

public final class NginxUtils {

	private NginxUtils() {
	}

	public static final Optional<String> getRealIp(Function<String, String> headerLoader) {
		String realIp = headerLoader.apply("X-Real-IP");
		String xForwardedFor = headerLoader.apply("X-Forwarded-For");
		if (!StringUtils.isEmpty(xForwardedFor)) {
			int i = xForwardedFor.indexOf(',');
			if (i > 0)
				realIp = xForwardedFor.substring(0, i);
			else
				realIp = xForwardedFor.trim();
		}
		return StringUtils.isEmpty(realIp) ? Optional.empty() : Optional.of(realIp);
	}

}

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

package com.newtranx.util.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("PropertyMapper")
public class PropertyMapper {

	@Autowired
	private ApplicationContext applicationContext;

	public Map<String, Object> startWith(String qualifier, String prefix) {
		return startWith(qualifier, prefix, false);
	}

	public Map<String, Object> startWith(String qualifier, String prefix, boolean removePrefix) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		Object obj = applicationContext.getBean(qualifier);
		if (obj instanceof Properties) {
			Properties mobileProperties = (Properties) obj;

			if (mobileProperties != null) {
				for (Entry<Object, Object> e : mobileProperties.entrySet()) {
					Object oKey = e.getKey();
					if (oKey instanceof String) {
						String key = (String) oKey;
						if (((String) oKey).startsWith(prefix)) {
							if (removePrefix)
								key = key.substring(prefix.length());
							result.put(key, e.getValue());
						}
					}
				}
			}
		}

		return result;
	}
}
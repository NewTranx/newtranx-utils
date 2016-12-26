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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Usage:<br/>
 * @Value("#{EnvPropertyMapper.startWith('prefix.', true)}") <br/>
 * private Map<String, Object> myProps;
 * 
 * @author luyi
 *
 */
@Component("EnvPropertyMapper")
public class EnvPropertyMapper {

	@Autowired
	private Environment env;

	public Map<String, Object> startWith(String prefix) {
		return startWith(prefix, false);
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Object> startWith(String prefix, boolean removePrefix) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		for (Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
			PropertySource propertySource = (PropertySource) it.next();
			processPropertySource(propertySource, prefix, result, removePrefix);
		}
		return result;
	}

	public Map<String, Object> subMap(String prefix) {
		return startWith(prefix, true);
	}

	public Properties subProperties(String prefix) {
		return toProperties(subMap(prefix));
	}

	public static Properties toProperties(Map<String, Object> map) {
		Properties prop = new Properties();
		for (Entry<String, Object> e : map.entrySet()) {
			prop.setProperty(e.getKey(), e.getValue().toString());
		}
		return prop;
	}

	@SuppressWarnings("rawtypes")
	private static void processPropertySource(PropertySource propertySource, String prefix, Map<String, Object> result,
			boolean removePrefix) {
		if (propertySource instanceof MapPropertySource) {
			MapPropertySource mapPropertySource = (MapPropertySource) propertySource;
			processMap(mapPropertySource, prefix, result, removePrefix);
		} else if (propertySource instanceof CompositePropertySource) {
			CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
			for (PropertySource sub : compositePropertySource.getPropertySources()) {
				processPropertySource(sub, prefix, result, removePrefix);
			}
		}
	}

	private static void processMap(MapPropertySource mapPropertySource, String prefix, Map<String, Object> result,
			boolean removePrefix) {
		for (Entry<String, Object> e : mapPropertySource.getSource().entrySet()) {
			String key = e.getKey();
			if (key.startsWith(prefix)) {
				if (removePrefix)
					key = key.substring(prefix.length());
				result.putIfAbsent(key, e.getValue());
			}
		}
	}

}

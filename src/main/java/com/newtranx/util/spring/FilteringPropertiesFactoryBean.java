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

import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;

public class FilteringPropertiesFactoryBean implements FactoryBean<Properties> {

	private Properties source;

	private String prefix;

	private Boolean removePrefix = true;

	@Override
	public Properties getObject() throws Exception {
		Properties newProps = new Properties();
		for (Entry<Object, Object> e : source.entrySet()) {
			String k = (String) e.getKey();
			String v = (String) e.getValue();
			if (k.startsWith(prefix)) {
				String newK = removePrefix ? k.substring(prefix.length()) : k;
				newProps.setProperty(newK, v);
			}
		}
		return newProps;
	}

	@Override
	public Class<Properties> getObjectType() {
		return Properties.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setSource(Properties source) {
		this.source = source;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setRemovePrefix(Boolean removePrefix) {
		this.removePrefix = removePrefix;
	}

}

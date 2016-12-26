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

import java.io.IOException;
import java.util.Properties;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.newtranx.util.zk.CuratorUtils;

public class ZkPropertiesFactoryBean extends PropertiesFactoryBean implements EnvironmentAware {

	private static final Logger logger = LogManager.getLogger(ZkPropertiesFactoryBean.class);

	private String zkLocationEnvName;

	private String zkLocation;

	private Environment environment;

	@Override
	protected Properties createProperties() throws IOException {
		Properties prop = super.createProperties();
		if (zkLocation == null || "".equals(zkLocation))
			zkLocation = environment.getProperty(this.zkLocationEnvName);
		if (zkLocation == null) {
			logger.warn("ZK connection string env var does not exist, skip zk based configuration.");
			return prop;
		}
		try {
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			CuratorFramework client = CuratorFrameworkFactory.newClient(zkLocation, retryPolicy);
			client.start();
			int count = CuratorUtils.loadToProperties(client, prop, "/");
			logger.info(String.format("Loaded %d properties from zookeeper.", count));
			client.close();
		} catch (Exception ex) {
			logger.warn("Failed to load configuration from ZK.", ex);
		}
		return prop;
	}

	public void setZkLocation(String zkLocation) {
		this.zkLocation = zkLocation;
	}

	public void setZkLocationEnvName(String zkLocationEnvName) {
		this.zkLocationEnvName = zkLocationEnvName;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

}

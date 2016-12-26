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

package com.newtranx.util.zk;

import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;

public class CuratorUtils {

	/**
	 * Convert a tree of znodes to a java.util.Properties. For example, given a
	 * tree having nodes /r/a, /r/a/b and /r/a/b/c, executing this method with
	 * {@code basePath="/r"} results in a properties which contains the
	 * following keys: a, a.b and a.b.c. The node without data will be ignored.
	 * Notice that the basePath is NOT included in the properties.
	 */
	public static Properties asProperties(CuratorFramework client, String basePath) throws Exception {
		Properties prop = new Properties();
		loadToProperties(client, prop, basePath);
		return prop;
	}

	/**
	 * See {@link #asProperties(CuratorFramework, String)}
	 * 
	 * @param out
	 *            the properties object for storing loaded properties
	 * @return number of entries loaded from zookeeper
	 */
	public static int loadToProperties(CuratorFramework client, Properties out, String basePath) throws Exception {
		return loadToProperties(client, out, basePath, "");
	}
	
	private static int loadToProperties(CuratorFramework client, Properties out, String basePath, String baseKey)
			throws Exception {
		int count = 0;
		List<String> children = client.getChildren().forPath(basePath);
		for (String child : children) {
			String childPath = Paths.get(basePath + "/" + child).normalize().toString();
			String childKey = baseKey + child;
			byte[] childDataRaw = client.getData().forPath(childPath);
			if (childDataRaw != null && childDataRaw.length > 0) {
				String childData = new String(childDataRaw, "utf8");
				out.put(childKey, childData);
				count++;
			}
			count += loadToProperties(client, out, childPath, childKey + ".");
		}
		return count;
	}

}

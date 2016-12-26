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

package com.newtranx.util.mysql.fabric;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysql.fabric.jdbc.FabricMySQLConnection;

@Component
public class SpringMybatisSetShardKeyUtilImpl implements SpringMybatisSetShardKeyUtil {

	private static final Logger log = LogManager.getLogger(SpringMybatisSetShardKeyUtilImpl.class);

	@Autowired
	private SqlSession sqlSession;

	@Override
	public void setShardKey(String shardKey, boolean force) throws SQLException {
		FabricMySQLConnection connection = (FabricMySQLConnection) sqlSession.getConnection();
		if (StringUtils.isEmpty(connection.getShardKey()) || force) {
			connection.setShardKey(shardKey);
			log.debug("New shardKey set");
			log.debug("ShardKey=" + shardKey);
			log.debug("CurrentServerGroup=" + connection.getCurrentServerGroup());
		} else {
			log.debug("Keep original shardKey");
			log.debug("ShardKey=" + connection.getShardKey());
		}
	}

}

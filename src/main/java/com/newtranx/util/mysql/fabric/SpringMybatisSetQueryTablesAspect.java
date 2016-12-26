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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysql.fabric.jdbc.FabricMySQLConnection;
import com.newtranx.util.aop.AspectJUtils;

@Component
@Aspect
public class SpringMybatisSetQueryTablesAspect {

	private static final Logger log = LogManager.getLogger(SpringMybatisSetQueryTablesAspect.class);

	@Autowired
	private SqlSession sqlSession;

	@Around("@annotation(com.newtranx.util.mysql.fabric.QueryTables)")
	public Object setQueryTables(ProceedingJoinPoint pjp) throws Throwable {
		try {
			Method method = AspectJUtils.getMethod(pjp);
			QueryTables qtAnnotation = method.getAnnotation(QueryTables.class);
			FabricMySQLConnection connection = (FabricMySQLConnection) sqlSession.getConnection();
			if ((connection.getQueryTables().isEmpty() && connection.getShardTable() == null) || qtAnnotation.reset()) {
				connection.clearServerSelectionCriteria();
				String[] tables = qtAnnotation.value();
				log.debug("Setting queryTables=" + Arrays.toString(tables));
				log.debug("Thread=" + Thread.currentThread() + ", conn=" + connection);
				if (qtAnnotation.useFirst()) {
					log.debug("Use setShardTable with first query table instead");
					connection.setShardTable(tables[0]);
					log.debug("New shardTable set");
				} else {
					for (String table : tables)
						connection.addQueryTable(table);
					log.debug("New queryTables set");
				}
			} else {
				log.debug("Keep original queryTables");
			}
			log.debug("QueryTables=" + connection.getQueryTables() + ", shardTable=" + connection.getShardTable());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw e;
		}
		return pjp.proceed();
	}

}

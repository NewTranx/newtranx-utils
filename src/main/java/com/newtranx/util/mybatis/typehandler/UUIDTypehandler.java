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

package com.newtranx.util.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedTypes(UUID.class)
public class UUIDTypehandler implements TypeHandler<UUID> {

	@Override
	public void setParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.toString());
	}

	@Override
	public UUID getResult(ResultSet rs, String columnName) throws SQLException {
		String uuidStr = rs.getString(columnName);
		if (uuidStr == null) {
			return null;
		}
		return UUID.fromString(uuidStr);
	}

	@Override
	public UUID getResult(ResultSet rs, int columnIndex) throws SQLException {
		String uuidStr = rs.getString(columnIndex);
		return UUID.fromString(uuidStr);
	}

	@Override
	public UUID getResult(CallableStatement cs, int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

}

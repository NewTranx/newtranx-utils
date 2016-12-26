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
import java.util.LinkedList;
import java.util.List;

import com.mysql.fabric.jdbc.FabricMySQLConnection;

public class ConnectionInitialization {

	@FunctionalInterface
	public interface Operation {
		void apply(FabricMySQLConnection connection) throws SQLException;
	}

	private List<Operation> ops = new LinkedList<>();

	public ConnectionInitialization addOperation(Operation op) {
		ops.add(op);
		return this;
	}

	public ConnectionInitialization doInit(Operation op) {
		addOperation(op);
		return this;
	}

	void apply(FabricMySQLConnection conn) throws SQLException {
		for (Operation initialization : ops) {
			initialization.apply(conn);
		}
	}

}

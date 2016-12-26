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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.fabric.jdbc.FabricMySQLConnection;
import com.mysql.fabric.jdbc.FabricMySQLDataSource;

public class FabricMySQLDataSourceEx extends FabricMySQLDataSource {

	private static final Logger log = LogManager.getLogger(FabricMySQLDataSourceEx.class);

	private static final long serialVersionUID = 4990560400184672463L;

	private static Object EMPTY = new Object();

	private ThreadLocal<ConnectionInitialization> connectionInitialization = ThreadLocal
			.withInitial(() -> new ConnectionInitialization());

	private ThreadLocal<Object> initRequired = new ThreadLocal<>();

	public ConnectionInitialization whenNewConnection() {
		initRequired.set(EMPTY);
		return this.connectionInitialization.get();
	}

	public void clearInitOps() {
		this.connectionInitialization.remove();
		this.initRequired.remove();
	}

	@Override
	protected Connection getConnection(Properties arg0) throws SQLException {
		long time0 = System.currentTimeMillis();
		Connection rawConnection = super.getConnection(arg0);
		FabricMySQLConnection connection = (FabricMySQLConnection) rawConnection;
		log.debug("Creating new connection: " + connection);
		long time1 = System.currentTimeMillis();
		if (initRequired.get() != null) {
			log.debug("Initializing connection");
			try {
				this.connectionInitialization.get().apply(connection);
			} finally {
				this.clearInitOps();
			}
		}
		log.debug("It took " + (time1 - time0) + "ms to create connection and " + (System.currentTimeMillis() - time1)
				+ "ms to initialize.");
		return connection;
	}

}

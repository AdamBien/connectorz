/*
 Copyright 2012 Adam Bien, adam-bien.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.connectorz.workmanager;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

/**
 * @author adam bien, adam-bien.com
 */
public class JCAExecutor implements WorkExecutor {

	private ConnectionRequestInfo connectionRequestInfo;

	private Connection genericManagedConnection;

	private LogWriter out;

	private ConnectionFactory gmcf;

	public JCAExecutor(LogWriter out, Connection genericManagedConnection, ConnectionFactory gmcf, ConnectionRequestInfo connectionRequestInfo) {
		this.out = out;
		out.println("#JCAWorkExecutor " + connectionRequestInfo + " " + toString());
		this.genericManagedConnection = genericManagedConnection;
		this.connectionRequestInfo = connectionRequestInfo;
		this.gmcf = gmcf;
	}

	@Override
	public void close() {
		out.println("#JCAWorkExecutor.close");
		this.genericManagedConnection.close();
	}

	@Override
	public void execute(final Runnable runnable) {
		Work work = new Work() {
			@Override
			public void run() {
				out.println("#JCAWorkExecutor.execute Executing Runnable");
				runnable.run();
				out.println("#JCAWorkExecutor.execute After Execution");
			}

			@Override
			public void release() {
			}
		};
		execute(work);
	}

	@Override
	public void execute(final Work work) {
		try {
			this.gmcf.getWorkManager().startWork(work);
		} catch (WorkException ex) {
			throw new IllegalStateException("Cannot execute work", ex);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JCAExecutor other = (JCAExecutor) obj;
		if (this.connectionRequestInfo != other.connectionRequestInfo && (this.connectionRequestInfo == null || !this.connectionRequestInfo.equals(other.connectionRequestInfo))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + (this.connectionRequestInfo != null ? this.connectionRequestInfo.hashCode() : 0);
		return hash;
	}
}

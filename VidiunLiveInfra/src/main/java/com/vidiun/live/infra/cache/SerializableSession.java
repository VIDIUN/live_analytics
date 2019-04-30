package com.vidiun.live.infra.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import com.vidiun.live.infra.exception.VidiunInternalException;

public class SerializableSession implements Externalizable {
	
	protected String node;
	protected Session session;
	private static Logger LOG = LoggerFactory.getLogger(SerializableSession.class);

	
	public SerializableSession() {
		node = "test";
	}
	public SerializableSession(String node) {
		this.node = node;
		session = SessionsCache.getSession(node);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(node);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		node = (String) in.readObject();		
		session = SessionsCache.getSession(node);
		
	}
	
	public Session getSession() {
		return session;
	}
	
	public void execute(Statement statement, int retries) throws VidiunInternalException {
		int retriesCount = 0;
		boolean success = false;
		do {
			try {
				++retriesCount;
				session.execute(statement);
				success = true;
			} catch (DriverException e) {
				if (retriesCount >= retries) {
					LOG.error("Failed to execute statement after " + retries + "retries with the following exception: " + e.getMessage()) ;
					throw new VidiunInternalException(e);
				}
				LOG.warn("Failed to execute statement, try #" + retriesCount + "with the following exception: "+ e.getMessage()) ;
			}
		} while (!success);
	}
	
	public void disconnect() {
		SessionsCache.disconnect(node);
	}
	

}

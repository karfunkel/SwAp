package org.aklein.swap.bus.channel;

/**
 * Default implementation of a {@link Channel} identified by its name
 */
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aklein.swap.bus.BusMessage;
import org.aklein.swap.bus.Channel;

public class DefaultChannel<T extends BusMessage> implements Channel<T> {
	private ReentrantLock	lock;
	private String			name;

	public DefaultChannel(String name) {
		super();
		this.name = name;
		lock = new ReentrantLock();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Lock getLock() {
		return lock;
	}
}

package org.aklein.swap.bus.channel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aklein.swap.bus.BusMessage;
import org.aklein.swap.bus.Channel;
import org.aklein.swap.bus.CombinedChannel;

public class DefaultCombinedChannel<T extends BusMessage> implements CombinedChannel<T> {
	private ReentrantLock	lock;
	private Channel<T>[]	channels;

	public DefaultCombinedChannel(Channel<T>... channels) {
		lock = new ReentrantLock();
		this.channels = channels;
	}

	public Channel<? extends T>[] getChannels() {
		return channels;
	}

	public Lock getLock() {
		return lock;
	}

}

package org.aklein.swap.bus.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aklein.swap.bus.BusMessage;
import org.aklein.swap.bus.Channel;
import org.aklein.swap.bus.ChannelFilter;
import org.aklein.swap.bus.ChannelListener;
import org.aklein.swap.bus.CombinedChannel;
import org.aklein.swap.bus.MessageBus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a MessageBus
 * 
 * @author Alexander Klein
 */
public class MessageBusImpl implements MessageBus {
	private static Log													log					= LogFactory.getLog(MessageBusImpl.class);

	private Lock														knownListenersLock	= new ReentrantLock();

	private Map<MultiKey, List<ChannelListener<? extends BusMessage>>>	knownListeners;

	private LinkedBlockingQueue<QueueEntry>								messageQueue		= new LinkedBlockingQueue<QueueEntry>();

	private MessageDispatchThread										mdt					= new MessageDispatchThread();

	public MessageBusImpl() {
		this.knownListeners = new HashMap<MultiKey, List<ChannelListener<? extends BusMessage>>>();
		mdt.start();
	}

	@SuppressWarnings("unchecked")
	public <B extends BusMessage, C extends Channel<B>> void subscribe(C channel, ChannelListener<B> listener, Object... additionalKeys) {
		// Check parameter
		if (channel == null || listener == null)
			return;

		if (additionalKeys.length > 4) {
			log.fatal("A maximum of 4 additional keys are allowed.");
			return;
		}

		// Some assertions
		assert this.knownListeners != null : "Listener must have been initialized!";

		// Return a list of all channel listeners
		this.knownListenersLock.lock();

		Set<Channel<? extends BusMessage>> channels = new HashSet<Channel<? extends BusMessage>>();
		if (channel instanceof CombinedChannel) {
			CombinedChannel cc = (CombinedChannel) channel;
			for (Channel<? extends BusMessage> c : cc.getChannels()) {
				channels.add(c);
			}
		}
		channels.add(channel);

		for (Channel<? extends BusMessage> c : channels) {
			List<ChannelListener<? extends BusMessage>> list = getListeners(c, additionalKeys);

			// Create a new list if we dont have any
			if (list == null) {
				list = new ArrayList<ChannelListener<? extends BusMessage>>();
				putListeners(c, additionalKeys, list);
			}

			// Finally add the listener
			if (!list.contains(listener))
				list.add(listener);
		}
		this.knownListenersLock.unlock();
	}

	private List<ChannelListener<? extends BusMessage>> getListeners(Channel<? extends BusMessage> c, Object[] additionalKeys) {
		MultiKey key = createKey(c, additionalKeys);
		return this.knownListeners.get(key);
	}

	private List<ChannelListener<? extends BusMessage>> getAllListeners(Channel<? extends BusMessage> c, Object[] additionalKeys) {
		List<ChannelListener<? extends BusMessage>> list = new ArrayList<ChannelListener<? extends BusMessage>>();
		mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, null, null, null)));
		if (additionalKeys.length > 0) {
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], null, null, null)));
		}
		if (additionalKeys.length > 1) {
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], additionalKeys[1], null, null)));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, additionalKeys[1], null, null)));
		}
		if (additionalKeys.length > 2) {
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], additionalKeys[1], additionalKeys[2], null)));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], null, additionalKeys[2], null)));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, additionalKeys[1], additionalKeys[2], null)));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, null, additionalKeys[2], null)));
		}
		if (additionalKeys.length > 3) {
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], additionalKeys[1], null, additionalKeys[3])));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], null, additionalKeys[2], additionalKeys[3])));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, additionalKeys[1], additionalKeys[2], additionalKeys[3])));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, additionalKeys[0], null, null, additionalKeys[3])));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, additionalKeys[1], null, additionalKeys[3])));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, null, additionalKeys[2], additionalKeys[3])));
			mergeListeners(list, this.knownListeners.get(new MultiKey(c, null, null, null, additionalKeys[3])));
		}
		return list;
	}

	private MultiKey createKey(Channel<? extends BusMessage> c, Object[] additionalKeys) {
		Object[] keys = new Object[4];
		for (int i = 0; i < 4; i++) {
			if (i < additionalKeys.length)
				keys[i] = additionalKeys[i];
			else
				keys[i] = null;
		}
		return new MultiKey(c, keys);
	}

	private void mergeListeners(List<ChannelListener<? extends BusMessage>> list, List<ChannelListener<? extends BusMessage>> adds) {
		if ((list == null) || (adds == null))
			return;
		for (ChannelListener<? extends BusMessage> channelListener : adds) {
			if (!list.contains(channelListener))
				list.add(channelListener);
		}
	}

	private void putListeners(Channel<? extends BusMessage> c, Object[] additionalKeys, List<ChannelListener<? extends BusMessage>> value) {
		MultiKey key = createKey(c, additionalKeys);
		this.knownListeners.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <B extends BusMessage, C extends Channel<B>> void unsubscribe(C channel, ChannelListener<B> listener, Object... additionalKeys) {
		// Check parameter
		if (channel == null || listener == null)
			return;

		if (additionalKeys.length > 4) {
			log.fatal("A maximum of 4 additional keys are allowed.");
			return;
		}

		// Some assertions
		assert this.knownListeners != null : "Lister must have been initialized!";

		// Return a list of all channel listeners
		this.knownListenersLock.lock();
		Set<Channel<? extends BusMessage>> channels = new HashSet<Channel<? extends BusMessage>>();
		if (channel instanceof CombinedChannel) {
			CombinedChannel cc = (CombinedChannel) channel;
			for (Channel<? extends BusMessage> c : cc.getChannels()) {
				channels.add(c);
			}
		}
		channels.add(channel);

		for (Channel<? extends BusMessage> c : channels) {
			List<ChannelListener<? extends BusMessage>> list = getListeners(c, additionalKeys);

			// Create a new list if we don't have any
			if (list != null)
				list.remove(listener);
		}
		this.knownListenersLock.unlock();
	}

	@SuppressWarnings("unchecked")
	public <B extends BusMessage, D extends B, C extends Channel<B>> void broadcast(final C channel, final D message, final Object... additionalKeys) {
		if (additionalKeys.length > 4) {
			log.fatal("A maximum of 4 additional keys are allowed.");
			return;
		}

		// Check parameter
		if (channel == null || message == null)
			return;

		messageQueue.offer(new QueueEntry(channel, message, additionalKeys));
		// Thread t = new Thread() {
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(100);
		// }
		// catch (InterruptedException e1) {}
		// channel.getLock().lock();
		// try {
		// // Obtain proper list
		// knownListenersLock.lock();
		// List<ChannelListener<? extends BusMessage>> list = getAllListeners(channel, additionalKeys);
		// if (list == null) {
		// knownListenersLock.unlock();
		// // c.getLock().unlock();
		// return;
		// }
		// knownListenersLock.unlock();
		//
		// // And propagate message.
		// for (ChannelListener channelListener : list) {
		// try {
		// ChannelFilter<D> filter = channelListener.getFilter();
		// if (filter == null || filter.isResponsible(message))
		// channelListener.incomingMessage(message);
		// }
		// catch (Exception e) {
		// // In case of an error, ensure the other listener will receive their event.
		// log.error(this.getClass().getName(), e);
		// }
		// }
		// }
		// finally {
		// channel.getLock().unlock();
		// }
		//
		// }
		// };
		// t.start();
	}

	@Override
	protected void finalize() throws Throwable {
		mdt.end();
		super.finalize();
	}

	private static class MultiKey {
		Channel<? extends BusMessage>	channel;
		Object[]						additionalKeys;

		public MultiKey(Channel<? extends BusMessage> channel, Object... additionalKeys) {
			super();
			this.channel = channel;
			this.additionalKeys = additionalKeys;
		}

		public Channel<? extends BusMessage> getChannel() {
			return channel;
		}

		public Object[] getAdditionalKeys() {
			return additionalKeys;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(additionalKeys);
			result = prime * result + ((channel == null) ? 0 : channel.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final MultiKey other = (MultiKey) obj;
			if (!Arrays.equals(additionalKeys, other.additionalKeys))
				return false;
			if (channel == null) {
				if (other.channel != null)
					return false;
			}
			else if (!channel.equals(other.channel))
				return false;
			return true;
		}

	}

	protected class QueueEntry<B extends BusMessage, D extends B, C extends Channel<B>> {
		private final D			message;
		private final Object[]	additionalKeys;
		private final C			channel;

		public QueueEntry(C channel, D message, Object... additionalKeys) {
			this.message = message;
			this.additionalKeys = additionalKeys;
			this.channel = channel;
		}

		public D getMessage() {
			return message;
		}

		public Object[] getAdditionalKeys() {
			return additionalKeys;
		}

		public C getChannel() {
			return channel;
		}
	}

	protected class MessageDispatchThread extends Thread {
		private volatile boolean	running	= false;

		@Override
		public void run() {
			while (running) {
				QueueEntry entry = null;
				try {
					entry = messageQueue.take();
				}
				catch (InterruptedException e) {
					log.error("BlockingQueue interrupted", e);
					Thread.yield();
					continue;
				}
				entry.getChannel().getLock().lock();
				try {
					// Obtain proper list
					knownListenersLock.lock();
					List<ChannelListener<? extends BusMessage>> list = getAllListeners(entry.getChannel(), entry.getAdditionalKeys());
					if (list == null) {
						knownListenersLock.unlock();
						return;
					}
					knownListenersLock.unlock();

					// And propagate message.
					for (ChannelListener channelListener : list) {
						try {
							ChannelFilter filter = channelListener.getFilter();
							if (filter == null || filter.isResponsible(entry.getMessage()))
								channelListener.incomingMessage(entry.getMessage());
						}
						catch (Exception e) {
							// In case of an error, ensure the other listener will receive their event.
							log.error(this.getClass().getName(), e);
						}
						Thread.yield();
					}
				}
				finally {
					entry.getChannel().getLock().unlock();
				}
			}
		}

		@Override
		public synchronized void start() {
			running = true;
			super.start();
		}

		public void end() {
			running = false;
		}
	}
}

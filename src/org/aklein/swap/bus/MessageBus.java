package org.aklein.swap.bus;

/**
 * The MessageBus is an global, independent event and message system.<br/> To guarantee, that Instance instatiated by
 * spring are really independent, they cannot addListener to each other, especially, because it is not know which
 * instance exists.<br/> Look at it as a kind of loadtime configured plugins.<br/> A ChannelListener can subscribe to
 * a Channel and if on this Channel Messages are broadcasted, <br/> all registered listeners will receive it, if it is
 * responsible for it. (defined by the ChannelListeners ChannelFilter)
 * 
 * @author Alexander Klein
 */
public interface MessageBus {
	/**
	 * Subscribe a {@link ChannelListener} to a {@link Channel} that can handle the same {@link BusMessage}
	 * 
	 * @param <B>
	 * @param <C>
	 * @param channel
	 * @param listener
	 * @param additionalKeys optional additional keys to optimise listener access
	 */
	public <B extends BusMessage, C extends Channel<B>> void subscribe(C channel, ChannelListener<B> listener, Object... additionalKeys);

	/**
	 * Unsubscribe a {@link ChannelListener} from a {@link Channel} that can handle the same {@link BusMessage}
	 * 
	 * @param <B>
	 * @param <C>
	 * @param channel
	 * @param listener
	 * @param additionalKeys optional additional keys to optimise listener access
	 */
	public <B extends BusMessage, C extends Channel<B>> void unsubscribe(C channel, ChannelListener<B> listener, Object... additionalKeys);

	/**
	 * Broadcast a {@link BusMessage} on a {@link Channel} that can handle it.
	 * 
	 * @param <B>
	 * @param <D>
	 * @param <C>
	 * @param channel
	 * @param message
	 * @param additionalKeys optional additional keys to optimise listener access
	 */
	public <B extends BusMessage, D extends B, C extends Channel<B>> void broadcast(C channel, D message, Object... additionalKeys);
}

package org.aklein.swap.bus;

/**
 * Interface for a listener to listen on a {@link Channel} for the {@link MessageBus}
 * 
 * @author Alexander Klein
 * 
 */
public interface ChannelListener<M extends BusMessage>
{
	/**
	 * method to handle the incomingMesaage
	 * 
	 * @param message
	 */
	public void incomingMessage(M message);

	/**
	 * Returns a {@link ChannelFilter} to check for message responsibility.<br/>
	 * 
	 * If null is returned, all messeges are valid.
	 * 
	 * @return
	 */
	public ChannelFilter<M> getFilter();
}

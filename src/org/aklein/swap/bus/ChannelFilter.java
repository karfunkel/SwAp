package org.aklein.swap.bus;

import org.aklein.swap.util.Filter;

/**
 * Interface for a filter, that checks if a {@link ChannelListener} is responsible for a incoming Message
 * 
 * @author Alexander Klein
 * 
 */
public interface ChannelFilter<T> extends Filter<T>
{
}

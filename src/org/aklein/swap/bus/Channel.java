package org.aklein.swap.bus;

import java.util.concurrent.locks.Lock;

/**
 * Marker interface for a Channel for the MessageBus
 * 
 * @author Alexander Klein
 * 
 */
public interface Channel<M extends BusMessage> {
    public Lock getLock();
}

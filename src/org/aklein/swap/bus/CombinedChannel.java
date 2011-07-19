package org.aklein.swap.bus;

public interface CombinedChannel<M extends BusMessage> extends Channel<M> {
    public Channel<? extends M>[] getChannels();
}

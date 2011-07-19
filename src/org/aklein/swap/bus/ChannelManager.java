package org.aklein.swap.bus;

import java.util.NoSuchElementException;

import org.aklein.swap.bus.ChannelMap.ScanException;

public class ChannelManager {
    private ChannelMap tail;

    private MessageBus bus;

    public ChannelManager(ChannelMap[] maps) {
        if (maps.length == 0)
            tail = new ChannelMap();
        else {
            ChannelMap parent = null;
            for (ChannelMap map : maps) {
                map.setParent(parent);
                parent = map;
            }
            tail = parent;
        }
    }

    public ChannelManager(Object[] objects) throws ScanException {
        this(null, objects);
    }

    public ChannelManager(ChannelMap parent, Object[] objects) throws ScanException {
        tail = new ChannelMap(parent);
        for (Object obj : objects) {
            tail.scanForAnnotations(obj);
        }
    }

    public Channel<? extends BusMessage> getChannel(String name) {
        return tail.getChannel(name);
    }

    public ChannelFilter<? extends BusMessage> getChannelFilter(String name) {
        return tail.getChannelFilter(name);
    }

    public ChannelListener<? extends BusMessage> getChannelListener(String name) {
        return tail.getChannelListener(name);
    }

    protected ChannelMap getTail() {
        return tail;
    }

    protected ChannelMap getRoot() {
        ChannelMap root = tail;
        while (root.getParent() != null)
            root = root.getParent();
        return root;
    }

    public MessageBus getBus() {
        return bus;
    }

    public void setBus(MessageBus bus) {
        this.bus = bus;
    }

    public void broadcast(String channel, BusMessage message, Object... additionalKeys) {
        if (getBus() == null)
            throw new NoSuchElementException("No messagebus registered with this ChannelManager");
        broadcast(getBus(), channel, message, additionalKeys);
    }

    public void subscribe(String channel, String listener, Object... additionalKeys) {
        if (getBus() == null)
            throw new NoSuchElementException("No messagebus registered with this ChannelManager");
        subscribe(getBus(), channel, listener, additionalKeys);
    }

    public void unsubscribe(String channel, String listener, Object... additionalKeys) {
        if (getBus() == null)
            throw new NoSuchElementException("No messagebus registered with this ChannelManager");
        unsubscribe(getBus(), channel, listener, additionalKeys);
    }

    @SuppressWarnings("unchecked")
    public <B extends BusMessage, D extends B, C extends Channel<B>>void broadcast(MessageBus bus, String channel, D message, Object... additionalKeys) {
        bus.broadcast((C) getChannel(channel), message, additionalKeys);
    }

    @SuppressWarnings("unchecked")
    public <B extends BusMessage, C extends Channel<B>>void subscribe(MessageBus bus, String channel, String listener, Object... additionalKeys) {
        bus.subscribe((C) getChannel(channel), (ChannelListener<B>) getChannelListener(listener), additionalKeys);
    }

    @SuppressWarnings("unchecked")
    public <B extends BusMessage, C extends Channel<B>>void unsubscribe(MessageBus bus, String channel, String listener, Object... additionalKeys) {
        bus.unsubscribe((C) getChannel(channel), (ChannelListener<B>) getChannelListener(listener), additionalKeys);
    }
}

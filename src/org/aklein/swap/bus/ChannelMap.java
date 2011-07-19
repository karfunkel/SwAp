package org.aklein.swap.bus;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ChannelMap {
    public final Map<String, Channel<? extends BusMessage>> channels = new HashMap<String, Channel<? extends BusMessage>>();
    public final Map<String, ChannelFilter<? extends BusMessage>> channelFilter = new HashMap<String, ChannelFilter<? extends BusMessage>>();
    public final Map<String, ChannelListener<? extends BusMessage>> channelListener = new HashMap<String, ChannelListener<? extends BusMessage>>();

    private ChannelMap parent;

    public ChannelMap() {
	super();
    }

    public ChannelMap(ChannelMap parent) {
	this();
	this.parent = parent;
    }

    public ChannelMap getParent() {
	return parent;
    }

    public void setParent(ChannelMap parent) {
	this.parent = parent;
    }

    public Channel<? extends BusMessage> registerChannel(String name, Channel<? extends BusMessage> value) {
	return channels.put(name, value);
    }

    public Channel<? extends BusMessage> unregisterChannel(String name) {
	return channels.remove(name);
    }

    public void unregisterChannel(Channel<? extends BusMessage> value) {
	for (String key : new HashSet<String>(channels.keySet())) {
	    Channel<? extends BusMessage> channel = channels.get(key);
	    if (channel.equals(value))
		channels.remove(key);
	}
    }

    public Channel<? extends BusMessage> getChannel(String name) {
	Channel<? extends BusMessage> channel = channels.get(name);
	if (channel == null && getParent() != null)
	    channel = getParent().getChannel(name);
	return channel;
    }

    public void clearChannels() {
	channels.clear();
    }

    public ChannelFilter<? extends BusMessage> registerChannelFilter(String name, ChannelFilter<? extends BusMessage> value) {
	return channelFilter.put(name, value);
    }

    public ChannelFilter<? extends BusMessage> unregisterChannelFilter(String name) {
	return channelFilter.remove(name);
    }

    public void unregisterChannelFilter(ChannelFilter<? extends BusMessage> value) {
	for (String key : new HashSet<String>(channelFilter.keySet())) {
	    ChannelFilter<? extends BusMessage> filter = channelFilter.get(key);
	    if (filter.equals(value))
		channelFilter.remove(key);
	}
    }

    public ChannelFilter<? extends BusMessage> getChannelFilter(String name) {
	ChannelFilter<? extends BusMessage> cf = channelFilter.get(name);
	if (cf == null && getParent() != null)
	    cf = getParent().getChannelFilter(name);
	return cf;
    }

    public void clearChannelFilters() {
	channelFilter.clear();
    }

    public ChannelListener<? extends BusMessage> registerChannelListener(String name, ChannelListener<? extends BusMessage> value) {
	return channelListener.put(name, value);
    }

    public ChannelListener<? extends BusMessage> unregisterChannelListener(String name) {
	return channelListener.remove(name);
    }

    public void unregisterChannelListener(ChannelListener<? extends BusMessage> value) {
	for (String key : new HashSet<String>(channelListener.keySet())) {
	    ChannelListener<? extends BusMessage> listener = channelListener.get(key);
	    if (listener.equals(value))
		channelListener.remove(key);
	}
    }

    public ChannelListener<? extends BusMessage> getChannelListener(String name) {
	ChannelListener<? extends BusMessage> cl = channelListener.get(name);
	if (cl == null && getParent() != null)
	    cl = getParent().getChannelListener(name);
	return cl;
    }

    public void clearChannelListeners() {
	channelListener.clear();
    }

    public void scanForAnnotations(Object source) throws ScanException {
	scanForAnnotations(source, source.getClass(), null);
    }

    public void scanForAnnotations(Object source, Class<?> startClass, Class<?> stopClass) throws ScanException {
	List<Class> classes = new ArrayList<Class>();
	for (Class c = startClass;; c = c.getSuperclass()) {
	    classes.add(c);
	    if (c.equals(stopClass)) {
		break;
	    }
	}
	Collections.reverse(classes);
	for (Class cls : classes) {
	    scanForAnnotations(source, cls);
	}
    }

    @SuppressWarnings("unchecked")
    protected void scanForAnnotations(Object source, Class<?> cls) throws ScanException {
	Field[] fields = cls.getDeclaredFields();
	for (Field field : fields) {
	    Object obj = null;
	    try {
		obj = field.get(source);
	    } catch (IllegalArgumentException e) {
		throw new ScanException("Field Exception: " + field.toGenericString(), e);
	    } catch (IllegalAccessException e) {
		throw new ScanException("Field Exception: " + field.toGenericString(), e);
	    }

	    ChannelDef cd = field.getAnnotation(ChannelDef.class);
	    if (cd != null) {
		if (!(obj instanceof Channel))
		    throw new ScanException("Field is no instance of org.aklein.swap.bus.Channel: " + field.toGenericString());
		registerChannel(cd.name(), (Channel<? extends BusMessage>) obj);
	    }
	    ChannelFilterDef cfd = field.getAnnotation(ChannelFilterDef.class);
	    if (cfd != null) {
		if (!(obj instanceof ChannelFilter))
		    throw new ScanException("Field is no instance of org.aklein.swap.bus.ChannelFilter: " + field.toGenericString());
		registerChannelFilter(cfd.name(), (ChannelFilter<? extends BusMessage>) obj);
	    }
	    ChannelListenerDef cld = field.getAnnotation(ChannelListenerDef.class);
	    if (cld != null) {
		if (!(obj instanceof ChannelListener))
		    throw new ScanException("Field is no instance of org.aklein.swap.bus.ChannelListener: " + field.toGenericString());
		registerChannelListener(cld.name(), (ChannelListener<? extends BusMessage>) obj);
	    }
	}

	Method[] methods = cls.getDeclaredMethods();
	for (Method method : methods) {
	    Class<?>[] args = method.getParameterTypes();
	    ChannelFilterDef cfd = method.getAnnotation(ChannelFilterDef.class);
	    ChannelListenerDef cld = method.getAnnotation(ChannelListenerDef.class);
	    if (cfd != null || cld != null) {
		if (args.length == 1 && args[0].equals(BusMessage.class)) {
		    if (cfd != null)
			registerChannelFilter(cfd.name(), new ProxyFilter(source, method));
		    if (cld != null)
			registerChannelListener(cld.name(), new ProxyListener(source, method, cld.filter()));
		} else {
		    throw new ScanException("Method may only have one argument of type BusMessage: " + method.toGenericString());
		}
	    }
	}
    }

    private class ProxyFilter implements ChannelFilter<BusMessage> {
	private Object source;
	private Method method;

	public ProxyFilter(Object source, Method method) {
	    this.source = source;
	    this.method = method;
	}

	public boolean isResponsible(BusMessage obj) {
	    try {
		return (Boolean) method.invoke(source, obj);
	    } catch (IllegalArgumentException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    } catch (InvocationTargetException e) {
		throw new RuntimeException(e);
	    }
	}
    }

    private class ProxyListener implements ChannelListener<BusMessage> {
	private Object source;
	private Method method;
	private String filter;

	public ProxyListener(Object source, Method method, String filter) {
	    this.source = source;
	    this.method = method;
	    this.filter = filter;
	}

	@SuppressWarnings("unchecked")
	public ChannelFilter<BusMessage> getFilter() {
	    if (filter == null)
		return null;
	    return (ChannelFilter<BusMessage>) getChannelFilter(filter);
	}

	public void incomingMessage(BusMessage message) {
	    try {
		method.invoke(source, message);
	    } catch (IllegalArgumentException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    } catch (InvocationTargetException e) {
		throw new RuntimeException(e);
	    }
	}
    }

    public static class ScanException extends Exception {
	private static final long serialVersionUID = -3319107793965938135L;

	public ScanException(String message, Throwable cause) {
	    super(message, cause);
	}

	public ScanException(String message) {
	    super(message);
	}

	public ScanException(Throwable cause) {
	    super(cause);
	}
    }
}

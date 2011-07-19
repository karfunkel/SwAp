package org.aklein.swap.util;

import java.util.HashMap;
import java.util.Map;

import org.jdesktop.swingx.table.DefaultTableColumnModelExt;

public class ExtendableTableColumnModelExt extends DefaultTableColumnModelExt {

	private Map<Object, Object>	clientProperties	= new HashMap<Object, Object>();

	public synchronized void putClientProperty(Object key, Object value) {
		clientProperties.put(key, value);
	}

	public Object getClientProperty(Object key) {
		return clientProperties.get(key);
	}

	public Object removeClientProperty(Object key) {
		return clientProperties.remove(key);
	}

	public Map<Object, Object> getClientProperties() {
		return clientProperties;
	}

}

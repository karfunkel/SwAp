package org.aklein.swap.util.binding;

import com.jgoodies.binding.value.AbstractValueModel;

public class BindableMapValueAdapter<V> extends AbstractValueModel {
	private static final long	serialVersionUID	= 269382740859169798L;
	private BindableMap<V>		map;
	private String				key;

	public BindableMapValueAdapter(BindableMap<V> map, String key) {
		super();
		this.map = map;
		this.key = key;
	}

	public Object getValue() {
		return map.get(key);
	}

	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		V old = map.put(key, (V) value);
		fireValueChange(old, value);
	}

}

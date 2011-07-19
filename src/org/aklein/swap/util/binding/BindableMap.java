package org.aklein.swap.util.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jgoodies.binding.beans.Model;

public class BindableMap<V> extends Model implements Map<String, V> {
	private static final long	serialVersionUID	= 983020179680856704L;
	private Map<String, V>		delegate;

	public BindableMap() {
		this(new HashMap<String, V>());
	}

	public BindableMap(Map<String, V> delegate) {
		super();
		this.delegate = delegate;
	}

	/**
	 * @see java.util.Map#get(Object)
	 */
	public V get(Object property) {
		return delegate.get(property);
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		delegate.clear();
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	/**
	 * @param value
	 * @return
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	/**
	 * @return
	 * @see java.util.Map#entrySet()
	 */
	public Set<Entry<String, V>> entrySet() {
		return delegate.entrySet();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Map#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	/**
	 * @return
	 * @see java.util.Map#hashCode()
	 */
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * @return
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		return delegate.keySet();
	}

	/**
	 * To its normal function, this implementation fires a {@link PropertyChangeEvent}
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public V put(String key, V value) {
		V old = get(key);
		delegate.put(key, value);
		firePropertyChange(key, old, value);
		return old;
	}

	/**
	 * To its normal function, this implementation fires a {@link PropertyChangeEvent} for each set entry
	 * 
	 * @param m
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends V> m) {
		for (java.util.Map.Entry<? extends String, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public V remove(Object key) {
		return delegate.remove(key);
	}

	/**
	 * @return
	 * @see java.util.Map#size()
	 */
	public int size() {
		return delegate.size();
	}

	/**
	 * @return
	 * @see java.util.Map#values()
	 */
	public Collection<V> values() {
		return delegate.values();
	}

	public BindableMapValueAdapter<V> getValueModel(final String property) {
		final BindableMapValueAdapter<V> model = new BindableMapValueAdapter<V>(this, property);
		model.addValueChangeListener(new PropertyChangeListener() {
			private Object	lastOld	= null;
			private Object	lastNew	= null;

			public void propertyChange(PropertyChangeEvent evt) {
				if ((lastOld == evt.getOldValue()) && (lastNew == evt.getNewValue()))
					return;
				lastOld = evt.getOldValue();
				lastNew = evt.getNewValue();
				firePropertyChange(property, evt.getOldValue(), evt.getNewValue());
			}
		});
		addPropertyChangeListener(property, new PropertyChangeListener() {
			private Object	lastOld	= null;
			private Object	lastNew	= null;

			public void propertyChange(PropertyChangeEvent evt) {
				if ((lastOld == evt.getOldValue()) && (lastNew == evt.getNewValue()))
					return;
				lastOld = evt.getOldValue();
				lastNew = evt.getNewValue();
				model.fireValueChange(evt.getOldValue(), evt.getNewValue());
			}
		});
		return model;
	}
}
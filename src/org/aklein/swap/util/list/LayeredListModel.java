package org.aklein.swap.util.list;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class LayeredListModel<K, T> extends AbstractListModel implements List<T> {

	private static final long				serialVersionUID	= -5478323780290409784L;
	private Map<K, ListBasedListModel<T>>	modelMap			= new HashMap<K, ListBasedListModel<T>>();
	private List<K>							order				= new ArrayList<K>();
	private PropertyChangeSupport			pcSupport			= new PropertyChangeSupport(this);
	private ListBasedListModel<T>			current;
	private ListDataListener				listener			= new ListDataListener() {
																	public void contentsChanged(ListDataEvent e) {
																		fireContentsChanged(this, e.getIndex0(), e.getIndex1());
																	}

																	public void intervalAdded(ListDataEvent e) {
																		fireIntervalAdded(this, e.getIndex0(), e.getIndex1());
																	}

																	public void intervalRemoved(ListDataEvent e) {
																		fireIntervalRemoved(this, e.getIndex0(), e.getIndex1());
																	}
																};

	public LayeredListModel() {
		this(new ListBasedListModel<T>());
	}

	public LayeredListModel(List<T> list) {
		ListBasedListModel<T> root = new ListBasedListModel<T>(list);
		root.addListDataListener(listener);
		modelMap.put(null, root);
	}

	public ListBasedListModel<T> putLayer(K key) {
		return putLayer(key, new ArrayList<T>());
	}

	public ListBasedListModel<T> putLayer(K key, List<T> list) {
		return putLayer(key, list, this.order.size());
	}

	public ListBasedListModel<T> putLayer(K key, List<T> list, int order) {
		ListBasedListModel<T> newLayer = new ListBasedListModel<T>(list);
		ListBasedListModel<T> oldLayer = modelMap.put(key, newLayer);
		if (oldLayer != null)
			oldLayer.removeListDataListener(listener);
		newLayer.addListDataListener(listener);
		this.order.add(order, key);
		return oldLayer;
	}

	public ListBasedListModel<T> removeLayer(int order) {
		K key = this.order.remove(order);
		if (key == null)
			return null;
		return removeLayer(key);
	}

	public ListBasedListModel<T> removeLayer(K key) {
		order.remove(key);
		ListBasedListModel<T> old = modelMap.remove(key);
		if (old != null)
			old.removeListDataListener(listener);
		return old;
	}

	public ListBasedListModel<T> getLayer(K key) {
		return modelMap.get(key);
	}

	public ListBasedListModel<T> getLayer(int order) {
		K key = this.order.get(order);
		return getLayer(key);
	}

	public ListBasedListModel<T> getCurrentLayer() {
		if (current == null)
			current = getLayer(null);
		return current;
	}

	public void setRootAsCurrentLayer() {
		setCurrentLayer(null);
	}

	public void setCurrentLayer(K key) {
		ListBasedListModel<T> old = current;
		if (old.size() > 0)
			fireIntervalRemoved(this, 0, old.size() - 1);
		current = getLayer(key);
		if (current.size() > 0)
			fireIntervalAdded(this, 0, current.size() - 1);
		pcSupport.firePropertyChange("currentLayer", old, current);
	}

	public void setCurrentLayer(int order) {
		K key = this.order.get(order);
		setCurrentLayer(key);
	}

	public int getLayerCount() {
		return this.order.size();
	}

	public List<K> getLayerKeys() {
		return new ArrayList<K>(this.order);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcSupport.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcSupport.getPropertyChangeListeners();
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return pcSupport.getPropertyChangeListeners(propertyName);
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
	 */
	public boolean hasListeners(String propertyName) {
		return pcSupport.hasListeners(propertyName);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcSupport.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param index
	 * @param element
	 * @see org.aklein.swap.util.list.ListBasedListModel#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		getCurrentLayer().add(index, element);
	}

	/**
	 * @param e
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getCurrentLayer().add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getCurrentLayer().addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return getCurrentLayer().addAll(index, c);
	}

	/**
	 * @param l
	 * @see javax.swing.AbstractListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener l) {
		addListDataListener(l);
	}

	public void addListDataListener(K key, ListDataListener l) {
		getLayer(key).addListDataListener(l);
	}

	/**
	 * @see org.aklein.swap.util.list.ListBasedListModel#clear()
	 */
	public void clear() {
		getCurrentLayer().clear();
	}

	/**
	 * @param o
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return getCurrentLayer().contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getCurrentLayer().containsAll(c);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return getCurrentLayer().equals(obj);
	}

	/**
	 * @param index
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#get(int)
	 */
	public T get(int index) {
		return getCurrentLayer().get(index);
	}

	/**
	 * @param index
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return getCurrentLayer().getElementAt(index);
	}

	/**
	 * @return
	 * @see javax.swing.AbstractListModel#getListDataListeners()
	 */
	@Override
	public ListDataListener[] getListDataListeners() {
		return getListDataListeners();
	}

	public ListDataListener[] getListDataListeners(K key) {
		return getLayer(key).getListDataListeners();
	}

	/**
	 * @param <T>
	 * @param listenerType
	 * @return
	 * @see javax.swing.AbstractListModel#getListeners(java.lang.Class)
	 */
	@Override
	public <V extends EventListener> V[] getListeners(Class<V> listenerType) {
		return getListeners(listenerType);
	}

	public <V extends EventListener> V[] getListeners(K key, Class<V> listenerType) {
		return getLayer(key).getListeners(listenerType);
	}

	/**
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#getSize()
	 */
	public int getSize() {
		return getCurrentLayer().getSize();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getCurrentLayer().hashCode();
	}

	/**
	 * @param o
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getCurrentLayer().indexOf(o);
	}

	/**
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#isEmpty()
	 */
	public boolean isEmpty() {
		return getCurrentLayer().isEmpty();
	}

	/**
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#iterator()
	 */
	public Iterator<T> iterator() {
		return getCurrentLayer().iterator();
	}

	/**
	 * @param o
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return getCurrentLayer().lastIndexOf(o);
	}

	/**
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return getCurrentLayer().listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return getCurrentLayer().listIterator(index);
	}

	/**
	 * @param index
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#remove(int)
	 */
	public T remove(int index) {
		return getCurrentLayer().remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getCurrentLayer().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getCurrentLayer().removeAll(c);
	}

	/**
	 * @param l
	 * @see javax.swing.AbstractListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener l) {
		removeListDataListener(l);
	}

	public void removeListDataListener(K key, ListDataListener l) {
		getLayer(key).removeListDataListener(l);
	}

	/**
	 * @param c
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getCurrentLayer().retainAll(c);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return getCurrentLayer().set(index, element);
	}

	/**
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#size()
	 */
	public int size() {
		return getCurrentLayer().size();
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#subList(int, int)
	 */
	public List<T> subList(int fromIndex, int toIndex) {
		return getCurrentLayer().subList(fromIndex, toIndex);
	}

	/**
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#toArray()
	 */
	public Object[] toArray() {
		return getCurrentLayer().toArray();
	}

	/**
	 * @param <E>
	 * @param a
	 * @return
	 * @see org.aklein.swap.util.list.ListBasedListModel#toArray(E[])
	 */
	public <E> E[] toArray(E[] a) {
		return getCurrentLayer().toArray(a);
	}
}

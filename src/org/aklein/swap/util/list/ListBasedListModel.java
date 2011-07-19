package org.aklein.swap.util.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractListModel;

public class ListBasedListModel<T> extends AbstractListModel implements List<T> {
	private static final long	serialVersionUID	= 5260583250316240862L;
	private List<T>				delegate;

	public ListBasedListModel() {
		this(new ArrayList<T>());
	}

	public ListBasedListModel(List<T> list) {
		super();
		this.delegate = list;
	}

	public Object getElementAt(int index) {
		return delegate.get(index);
	}

	public int getSize() {
		return delegate.size();
	}

	public boolean add(T e) {
		int index = delegate.size();
		boolean result = delegate.add(e);
		fireIntervalAdded(this, index, index);
		return result;
	}

	public void add(int index, T element) {
		delegate.add(index, element);
		fireIntervalAdded(this, index, index);
	}

	public boolean addAll(Collection<? extends T> c) {
		boolean modified = false;
		Iterator<? extends T> e = c.iterator();
		while (e.hasNext()) {
			if (add(e.next()))
				modified = true;
		}
		return modified;
	}

	public boolean addAll(int index, Collection<? extends T> c) {
		boolean modified = false;
		Iterator<? extends T> e = c.iterator();
		while (e.hasNext()) {
			add(index++, e.next());
			modified = true;
		}
		return modified;
	}

	public void clear() {
		int index1 = delegate.size() - 1;
		delegate.clear();
		if (index1 >= 0) {
			fireIntervalRemoved(this, 0, index1);
		}
	}

	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	public T get(int index) {
		return delegate.get(index);
	}

	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return delegate.listIterator();
	}

	public ListIterator<T> listIterator(int index) {
		return delegate.listIterator(index);
	}

	public boolean remove(Object o) {
		int index = indexOf(o);
		boolean rv = delegate.remove(o);
		if (index >= 0) {
			fireIntervalRemoved(this, index, index);
		}
		return rv;
	}

	public T remove(int index) {
		T rv = delegate.remove(index);
		fireIntervalRemoved(this, index, index);
		return rv;
	}

	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<?> e = iterator();
		while (e.hasNext()) {
			if (c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		return modified;
	}

	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<T> e = iterator();
		while (e.hasNext()) {
			if (!c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		return modified;
	}

	public T set(int index, T element) {
		T rv = delegate.set(index, element);
		fireContentsChanged(this, index, index);
		return rv;
	}

	public int size() {
		return delegate.size();
	}

	public List<T> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		Object[] rv = new Object[delegate.size()];
		delegate.toArray(rv);
		return rv;
	}

	public <E> E[] toArray(E[] a) {
		return delegate.toArray(a);
	}

}

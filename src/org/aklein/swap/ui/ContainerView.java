package org.aklein.swap.ui;

import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ContainerView<C extends Container> extends AbstractView<C> {
	private PropertyChangeSupport	pcs	= new PropertyChangeSupport(this);
	private C						root;

	public ContainerView(ViewController<C> controller, C root) {
		super(controller);
		this.root = root;
	}

	@Override
	protected View<C> createView(C comp, ViewController<C> controller) {
		return new ContainerView<C>(controller, comp);
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return pcs.getPropertyChangeListeners(propertyName);
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
	 */
	public boolean hasListeners(String propertyName) {
		return pcs.hasListeners(propertyName);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}

	public C getContainer() {
		return root;
	}
}

package org.aklein.swap.ui;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractView<C extends Container> implements View<C> {
	private ViewController<C>		controller;
	protected Map<String, Object>	properties	= new HashMap<String, Object>();

	/**
	 * Instantiate a View with the controller
	 * 
	 * @param container
	 * @param controller
	 */
	public AbstractView(ViewController<C> controller) {
		this.controller = controller;
	}

	public ViewController<C> getController() {
		return controller;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * @see View#getProperty()
	 */
	public Object getProperty(String name) {
		fixPropertyName(name);
		Component comp = getComponentByName(name);
		if (comp != null)
			if (getContainer().getClass().isAssignableFrom(comp.getClass()))
				return createView((C) comp, controller);
			else
				return comp;
		try {
			Field field = getClass().getField(name);
			return field.get(this);
		}
		catch (Exception ex) {
			try {
				return getGetter(name).invoke(this, (Object[]) null);
			}
			catch (Exception ex1) {
				return getProperties().get(name);
			}
		}
	}

	protected abstract View<C> createView(C comp, ViewController<C> controller);

	protected void fixPropertyName(String name) {
		if (name.startsWith("."))
			name = name.substring(1);
	}

	/**
	 * @see View#setProperty()
	 */
	public void setProperty(String name, Object value) {
		fixPropertyName(name);
		try {
			Field field = getClass().getField(name);
			Object old = field.get(this);
			field.set(this, value);
			firePropertyChange(name, old, value);
		}
		catch (Exception ex) {
			try {
				Object old = getGetter(name).invoke(this, (Object[]) null);
				getSetter(name, value).invoke(this, value);
				Object val = getGetter(name).invoke(this, (Object[]) null);
				firePropertyChange(name, old, val);
			}
			catch (Exception ex1) {
				Object old = getProperty(name);
				getProperties().put(name, value);
				firePropertyChange(name, old, value);
			}
		}
	}

	protected Method getGetter(String name) throws SecurityException, NoSuchMethodException {
		return getGetter(this, name);
	}

	protected Method getGetter(Object bean, String name) throws SecurityException, NoSuchMethodException {
		char[] prop = name.toCharArray();
		prop[0] = Character.toUpperCase(prop[0]);
		Method method = null;
		try {
			method = bean.getClass().getMethod("get" + new String(prop), (Class[]) null);
		}
		catch (NoSuchMethodException e) {
			method = bean.getClass().getMethod("is" + new String(prop), (Class[]) null);
		}
		return method;
	}

	protected Method getSetter(String name, Object value) throws SecurityException, NoSuchMethodException {
		char[] prop = name.toCharArray();
		prop[0] = Character.toUpperCase(prop[0]);
		List<Class> cls = new ArrayList<Class>();
		Class c = value.getClass();
		cls.add(c);
		while (c.getSuperclass() != null) {
			c = c.getSuperclass();
			cls.add(c);
		}
		addInterfaces(cls, value.getClass());
		cls.add(Object.class);
		Method method = null;
		for (Class clazz : cls) {
			try {
				method = getClass().getMethod("set" + new String(prop), new Class[] { clazz });
				break;
			}
			catch (NoSuchMethodException e) {}
		}
		return method;
	}

	protected void addInterfaces(List<Class> list, Class c) {
		for (Class i : c.getInterfaces()) {
			list.add(i);
			addInterfaces(list, i);
		}
	}

	public Component getComponentByName(String name) {
		fixPropertyName(name);
		String[] parts = name.split("\\.");
		Component container = getContainer();
		for (String part : parts) {
			part = part.trim();
			if (part.length() < 1)
				continue;
			if (!(container instanceof Container))
				return null;
			container = getComponentByName((Container) container, part);
			if (container == null)
				return null;
		}
		if (container == getContainer())
			return null;
		return container;
	}

	protected Component getComponentByName(Container container, String name) {
		Component component = null;
		fixPropertyName(name);
		for (Component comp : container.getComponents()) {
			String cname = comp.getName();
			if (cname != null) {
				component = comp;
				break;
			}
		}
		if (component == null) {
			try {
				Method method = getGetter(container, name);
				Object result = method.invoke(container);
				if (result instanceof Component)
					component = (Component) result;
			}
			catch (Exception e) {}
		}
		return component;
	}

	protected abstract void firePropertyChange(String propertyName, Object oldValue, Object newValue);

}

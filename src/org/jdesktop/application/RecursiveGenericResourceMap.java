package org.jdesktop.application;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import org.apache.commons.beanutils.MethodUtils;
import org.jdesktop.application.ResourceMap;

public class RecursiveGenericResourceMap extends ResourceMap {

    private static Logger logger = Logger.getLogger(RecursiveGenericResourceMap.class.getName());

    public RecursiveGenericResourceMap(ResourceMap parent, ClassLoader classLoader, List<String> bundleNames) {
	super(parent, classLoader, bundleNames);
    }

    public RecursiveGenericResourceMap(ResourceMap parent, ClassLoader classLoader, String... bundleNames) {
	super(parent, classLoader, bundleNames);
    }

    @Override
    public void injectComponent(Component target) {
	if (target == null) {
	    throw new IllegalArgumentException("null target");
	}
	injectComponentProperties(target);
    }

    private void injectComponentProperties(Component component) {
	String componentName = component.getName();
	if (componentName != null) {
	    /*
	     * Optimization: punt early if componentName doesn't appear in any componentName.propertyName resource keys
	     */
	    boolean matchingResourceFound = false;
	    for (String key : keySet()) {
		int i = key.indexOf(".");
		if ((i != -1) && componentName.equals(key.substring(0, i))) {
		    matchingResourceFound = true;
		    break;
		}
	    }
	    if (!matchingResourceFound) {
		return;
	    }
	    for (String key : keySet()) {
		int i = key.indexOf(".");
		String keyComponentName = (i == -1) ? null : key.substring(0, i);
		if (componentName.equals(keyComponentName)) {
		    if ((i + 1) == key.length()) {
			/*
			 * key has no property name suffix, e.g. "myComponentName." This is probably a mistake.
			 */
			String msg = "component resource lacks property name suffix";
			logger.warning(msg);
			break;
		    }
		    injectDeepComponentProperties(component, key, key.substring(i + 1));
		}
	    }
	}
    }

    private void injectDeepComponentProperties(Object component, String key, String subKey) {
	int i = subKey.indexOf('.');
	if (i < 0) {
	    String propertyName = subKey.substring(i + 1);
	    Object old;
	    try {
		old = getValue(component, propertyName);
	    } catch (Exception e) {
		String msg = String.format("[resource %s] component named %s doesn't have a property named %s", key, subKey, propertyName);
		logger.warning(msg);
		return;
	    }
	    injectComponentProperty(component, propertyName, key, subKey);
	} else {
	    String propertyName = subKey.substring(0, i);
	    try {
		component = getValue(component, propertyName);
	    } catch (Exception e) {
		String msg = String.format("[resource %s] component named %s doesn't have a property named %s", key, subKey, propertyName);
		logger.warning(msg);
		return;
	    }
	    injectDeepComponentProperties(component, key, subKey.substring(i + 1));
	}
    }

    private Object getValue(Object component, String propertyName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	try {
	    String method = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	    return MethodUtils.invokeMethod(component, method, null);
	} catch (Exception e) {
	    try {
		String method = "is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		return MethodUtils.invokeMethod(component, method, null);
	    } catch (Exception e1) {
		try {
		    return MethodUtils.invokeMethod(component, "getProperty", propertyName);
		} catch (Exception e2) {
		    return MethodUtils.invokeMethod(component, "get", propertyName);
		}
	    }
	}
    }

    private Class getType(Object component, String propertyName) {
	try {
	    String method = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	    return MethodUtils.getMatchingAccessibleMethod(component.getClass(), method, null).getReturnType();
	} catch (Exception e) {
	    try {
		String method = "is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		return MethodUtils.getMatchingAccessibleMethod(component.getClass(), method, null).getReturnType();
	    } catch (Exception e1) {
		try {
		    return MethodUtils.getMatchingAccessibleMethod(component.getClass(), "getProperty", new Class[] { String.class }).getReturnType();
		} catch (Exception e2) {
		    return MethodUtils.getMatchingAccessibleMethod(component.getClass(), "get", new Class[] { String.class }).getReturnType();
		}
	    }
	}
    }

    private Object setValue(Object component, String propertyName, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	try {
	    String method = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	    return MethodUtils.invokeMethod(component, method, value);
	} catch (Exception e) {
	    try {
		return MethodUtils.invokeMethod(component, "setProperty", new Object[] { propertyName, value });
	    } catch (Exception e1) {
		return MethodUtils.invokeMethod(component, "set", new Object[] { propertyName, value });
	    }
	}
    }

    private void injectComponentProperty(Object component, String propertyName, String key, String componentName) {
	Class type = null;
	try {
	    type = getType(component, propertyName);
	} catch (Exception e1) {
	    String msg = String.format("[resource %s] component named %s doesn't have a property named %s", key, componentName, propertyName);
	    logger.warning(msg);
	}
	if ((type != null) && containsKey(key)) {
	    Object value = getObject(key, type);
	    try {
		// Note: this could be generalized, we could delegate
		// to a component property injector.
		if ("text".equals(propertyName) && (component instanceof AbstractButton)) {
		    MnemonicText.configure(component, (String) value);
		} else if ("text".equals(propertyName) && (component instanceof JLabel)) {
		    MnemonicText.configure(component, (String) value);
		} else {
		    try {
			setValue(component, propertyName, value);
		    } catch (Exception e) {
			int i = 0;
		    }
		}
	    } catch (Exception e) {
		String msg = "property setter failed";
		RuntimeException re = new PropertyInjectionException2(msg, key, component, propertyName);
		re.initCause(e);
		throw re;
	    }
	} else if (type != null) {
	    String msg = "no value specified for resource";
	    throw new PropertyInjectionException2(msg, key, component, propertyName);
	}
    }

    /**
     * Unchecked exception thrown by {@link #injectComponent} and {@link #injectComponents} when a property value
     * specified by a resource can not be set.
     * 
     * @see #injectComponent
     * @see #injectComponents
     */
    public static class PropertyInjectionException2 extends RuntimeException {
	private final String key;
	private final Object component;
	private final String propertyName;

	/**
	 * Constructs an instance of this class with some useful information about the failure.
	 * 
	 * @param msg
	 *            the detail message
	 * @param key
	 *            the name of the resource
	 * @param component
	 *            the component whose property couldn't be set
	 * @param propertyName
	 *            the name of the component property
	 */
	public PropertyInjectionException2(String msg, String key, Object component, String propertyName) {
	    super(String.format("%s: resource %s, property %s, component %s", msg, key, propertyName, component));
	    this.key = key;
	    this.component = component;
	    this.propertyName = propertyName;
	}

	/**
	 * Returns the the name of resource whose value was to be used to set the property
	 * 
	 * @return the resource name
	 */
	public String getKey() {
	    return key;
	}

	/**
	 * Returns the component whose property could not be set
	 * 
	 * @return the component
	 */
	public Object getComponent() {
	    return component;
	}

	/**
	 * Returns the the name of property that could not be set
	 * 
	 * @return the property name
	 */
	public String getPropertyName() {
	    return propertyName;
	}
    }

}

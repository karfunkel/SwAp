package org.aklein.swap.util.spring;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 * Utitity bean to help defining classes in spring configuration
 * 
 * @author Alexander Klein
 *
 */
public class SpringTools
{
	/**
	 * Get i18n message from the global {@link ResourceMap}
	 * @param key
	 * @return
	 */
	public String getResourceString(String key)
	{
		String txt = Application.getInstance().getContext().getResourceMap().getString(key);
		return txt == null ? "" : txt;
	}

	/**
	 * Get i18n message from the {@link ResourceMap} of teh given class
	 * @param key
	 * @param class
	 * @return
	 */
	public String getResourceString(String key, Class cls)
	{
		String txt = Application.getInstance().getContext().getResourceMap(cls).getString(key);
		return txt == null ? "" : txt;
	}

}

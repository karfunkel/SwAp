package org.aklein.swap.util.configuration;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 * This lookup allows i18n messages in configuration entries.<br/> <br/>
 * 
 * usage: ${message:class#key}<br/> <br/>
 * 
 * It uses the Resource mechanism of the AppFramework, using the {@link ResourceMap}<br/>
 * 
 * If class starts with / it is treated as an absolute class, complete with package. If not, the registered basePackage is prefixed to it before determining the corresponding {@link ResourceMap}.
 * 
 * @author Alexander Klein
 * 
 */
public class MessageLookup extends StrLookup
{
	private String basePackage;
	private static Log log = LogFactory.getLog(MessageLookup.class);

	/**
	 * Constructs the lookup
	 * 
	 * @param basePackage
	 *            To shorten the key length in the ${message:class#key} statements, this string is the prefix for the 'class' part when looking for the resources.
	 */
	public MessageLookup(String basePackage)
	{
		super();
		this.basePackage = basePackage;
	}

	/**
	 * Searches for the given key in the corresponding {@link ResourceMap}
	 *
	 * @param key
	 */
	@Override
	public String lookup(String key)
	{
		String[] keys = key.split(";");
		Object[] params = new Object[keys.length - 1];
		for (int i = 1; i < keys.length; i++)
			params[i - 1] = keys[i];
		String result = Application.getInstance().getContext().getResourceMap().getString(keys[0], params);
		if (result == null)
		{
			int pos = keys[0].indexOf('#');
			if (pos < 0)
				result = null;
			try
			{
				Class cls;
				if (keys[0].startsWith("/"))
					cls = Class.forName(keys[0].substring(0, pos));
				else
					cls = Class.forName(basePackage + "." + keys[0].substring(0, pos));
				String subKey = keys[0].substring(pos + 1);
				result = Application.getInstance().getContext().getResourceMap(cls).getString(subKey, params);
			}
			catch (ClassNotFoundException e)
			{
				result = null;
			}
		}
		if (result == null)
			log.warn("Key '" + keys[0] + "' for request '" + key + "' not found ");
		return result;
	}
}

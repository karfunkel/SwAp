package org.apache.commons.configuration;

/**
 * Helper to get access to protected method 
 * 
 * @author Alexander Klein
 *
 */
public class CustomConverter
{
	public static Object to(Class cls, Object value, String... params)
	{
		return PropertyConverter.to(cls, value, params);
	}

}

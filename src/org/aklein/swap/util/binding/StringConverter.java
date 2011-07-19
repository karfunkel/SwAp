/*
 * Created on 13.06.2006
 */
package org.aklein.swap.util.binding;

import java.text.ParseException;

import javax.swing.JFormattedTextField;

import com.jgoodies.binding.value.AbstractConverter;
import com.jgoodies.binding.value.ValueModel;

/**
 * Converts Values to Strings and vice-versa using a given Format.
 */
public class StringConverter extends AbstractConverter
{
	private static final long serialVersionUID = -8120412466970727083L;
	/**
	 * Holds the <code>Format</code> used to format and parse.
	 */
	private final JFormattedTextField.AbstractFormatter formatter;

	// Instance Creation **************************************************

	/**
	 * Constructs a <code>StringConverter</code> on the given subject using the specified <code>Format</code>.
	 * 
	 * @param subject
	 *            the underlying ValueModel.
	 * @param format
	 *            the <code>Format</code> used to format and parse
	 * @throws NullPointerException
	 *             if the subject or the format is null.
	 */
	public StringConverter(ValueModel subject, JFormattedTextField.AbstractFormatter formatter)
	{
		super(subject);
		if (formatter == null)
		{
			throw new NullPointerException("The format must not be null.");
		}
		this.formatter = formatter;
	}

	// Implementing Abstract Behavior *************************************

	/**
	 * Formats the subject value and returns a String representation.
	 * 
	 * @param subjectValue
	 *            the subject's value
	 * @return the formatted subjectValue
	 */
	public Object convertFromSubject(Object subjectValue)
	{
		try
		{
			return formatter.valueToString(subjectValue);
		}
		catch (ParseException e)
		{
			return null;
		}
	}

	// Implementing ValueModel ********************************************

	/**
	 * Parses the given String encoding and sets it as the subject's new value. Silently catches <code>ParseException</code>.
	 * 
	 * @param value
	 *            the value to be converted and set as new subject value
	 */
	public void setValue(Object value)
	{
		try
		{
			subject.setValue(formatter.stringToValue((String) value));
		}
		catch (ParseException e)
		{
			// Do not change the subject value
		}
	}

}

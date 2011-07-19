/*
 * Created on 05.06.2006
 *
 */
package org.aklein.swap.util.validation;

import java.util.Date;

import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.message.AbstractValidationMessage;
import com.jgoodies.validation.util.ValidationUtils;

/**
 * An implementation of {@link ValidationMessage} that just holds a text with a timestamp together with a link to the rule in validation.xml.
 * 
 * @author Alexander Klein
 */
public class CustomValidationMessage<K> extends AbstractValidationMessage
{
	private static final long serialVersionUID = -6767760147114139223L;
	Date timeStamp;
	K ruleKey;

	// Instance Creation ******************************************************

	public CustomValidationMessage(String text, Severity severity, K key, K ruleKey)
	{
		super(text, severity, key);
		timeStamp = new Date();
		this.ruleKey = ruleKey;
		if (text == null)
			throw new NullPointerException("The text must not be null");
	}

	// Comparison and Hashing *************************************************

	/**
	 * Compares the specified object with this validation message for equality. Returns <code>true</code> if and only if the specified object is also a simple validation message, both messages have
	 * the same severity, key, and formatted text. In other words, two simple validation messages are defined to be equal if and only if they behave one like the other.
	 * <p>
	 * 
	 * This implementation first checks if the specified object is this a simple validation message. If so, it returns <code>true</code>; if not, it checks if the specified object is a simple
	 * validation message. If not, it returns <code>false</code>; if so, it checks and returns if the severities, keys and formatted texts of both messages are equal.
	 * 
	 * @param o
	 *            the object to be compared for equality with this validation message.
	 * 
	 * @return <code>true</code> if the specified object is equal to this validation message.
	 * 
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof CustomValidationMessage))
			return false;
		CustomValidationMessage other = (CustomValidationMessage) o;
		return severity().equals(other.severity()) && ValidationUtils.equals(key(), other.key()) && ValidationUtils.equals(formattedText(), other.formattedText());
	}

	/**
	 * Returns the hash code value for this validation message. This implementation computes and returns the hash based on the hash code values of this messages' severity, key, and text.
	 * 
	 * @return the hash code value for this validation message.
	 * 
	 * @see Object#hashCode()
	 */
	public int hashCode()
	{
		String formattedText = formattedText();
		int result = 17;
		result = 37 * result + severity().hashCode();
		result = 37 * result + (key() == null ? 0 : key().hashCode());
		result = 37 * result + (getRuleKey() == null ? 0 : getRuleKey().hashCode());
		result = 37 * result + (formattedText == null ? 0 : formattedText.hashCode());
		return result;
	}

	public Date getTimeStamp()
	{
		return timeStamp;
	}

	public K getRuleKey()
	{
		return ruleKey;
	}
}

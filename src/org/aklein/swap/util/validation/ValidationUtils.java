/*
 * Created on 23.06.2006
 */
package org.aklein.swap.util.validation;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Consists exclusively of static methods that provide convenience behavior for validating input values by testing and comparing single and multiple values.
 * <p>
 * 
 * If you are interested in a richer set of validation operations, you may consider looking at the <em>Lang</em> component of the <a
 * href="http://jakarta.apache.org/commons/lang.html">Jakarta Commons</a>.
 * 
 * @author Karsten Lentzsch
 * @version 1.1.2.1
 * 
 * @see Calendar
 */

public class ValidationUtils
{

	private ValidationUtils()
	{
	// Override default constructor; prevents instantiation.
	}

	// Object Comparison ******************************************************

	/**
	 * Checks and answers if the two objects are both <code>null</code> or equal.
	 * 
	 * <pre>
	 *     ValidationUtils.equals(null, null)  == true
	 *     ValidationUtils.equals(&quot;Hi&quot;, &quot;Hi&quot;)  == true
	 *     ValidationUtils.equals(&quot;Hi&quot;, null)  == false
	 *     ValidationUtils.equals(null, &quot;Hi&quot;)  == false
	 *     ValidationUtils.equals(&quot;Hi&quot;, &quot;Ho&quot;)  == false
	 * </pre>
	 * 
	 * @param o1
	 *            the first object to compare
	 * @param o2
	 *            the second object to compare
	 * @return boolean <code>true</code> if and only if both objects are <code>null</code> or equal
	 */
	public static boolean equals(Object o1, Object o2)
	{
		return (o1 != null && o2 != null && o1.equals(o2)) || (o1 == null && o2 == null);
	}

	// String Validations ***************************************************

	/**
	 * Checks and answers if the given string is empty (<code>""</code>) or <code>null</code>.
	 * 
	 * <pre>
	 *     ValidationUtils.isEmpty(null)  == true
	 *     ValidationUtils.isEmpty(&quot;&quot;)    == true
	 *     ValidationUtils.isEmpty(&quot; &quot;)   == false
	 *     ValidationUtils.isEmpty(&quot;Hi &quot;) == false
	 * </pre>
	 * 
	 * @param str
	 *            the string to check, may be <code>null</code>
	 * @return <code>true</code> if the string is empty or <code>null</code>
	 * @see #isBlank(String)
	 */
	public static boolean isEmpty(String str)
	{
		return str == null || str.length() == 0;
	}

	/**
	 * Checks and answers if the given string is whitespace, empty (<code>""</code>) or <code>null</code>.
	 * 
	 * <pre>
	 *     ValidationUtils.isBlank(null)  == true
	 *     ValidationUtils.isBlank(&quot;&quot;)    == true
	 *     ValidationUtils.isBlank(&quot; &quot;)   == true
	 *     ValidationUtils.isBlank(&quot;Hi &quot;) == false
	 * </pre>
	 * 
	 * @param str
	 *            the string to check, may be <code>null</code>
	 * @return <code>true</code> if the string is whitespace, empty or <code>null</code>
	 * @see #isEmpty(String)
	 */
	public static boolean isBlank(Object obj)
	{
		if (obj instanceof String)
			return obj == null || ((String) obj).trim().length() == 0;
		return obj == null;
	}

	/**
	 * Checks and answers if the given string has at least the specified minimum length. Strings that are <code>null</code> or contain only blanks have length
	 * 0.
	 * 
	 * <pre>
	 *     ValidationUtils.hasMinimumLength(null,  2) == false
	 *     ValidationUtils.hasMinimumLength(&quot;&quot;,    2) == false
	 *     ValidationUtils.hasMinimumLength(&quot; &quot;,   2) == false
	 *     ValidationUtils.hasMinimumLength(&quot;   &quot;, 2) == false
	 *     ValidationUtils.hasMinimumLength(&quot;Hi &quot;, 2) == true
	 *     ValidationUtils.hasMinimumLength(&quot;Ewa&quot;, 2) == true
	 * </pre>
	 * 
	 * @param str
	 *            the string to check
	 * @param min
	 *            the minimum length
	 * @return <code>true</code> if the length is greater or equal to the minimum, <code>false</code> otherwise
	 */
	public static boolean hasMinimumLength(String str, int min)
	{
		int length = str == null ? 0 : str.trim().length();
		return min <= length;
	}

	/**
	 * Checks and answers if the given string is shorter than the specified maximum length. Strings that are <code>null</code> or contain only blanks have
	 * length 0.
	 * 
	 * <pre>
	 *     ValidationUtils.hasMaximumLength(null,  2) == true
	 *     ValidationUtils.hasMaximumLength(&quot;&quot;,    2) == true
	 *     ValidationUtils.hasMaximumLength(&quot; &quot;,   2) == true
	 *     ValidationUtils.hasMaximumLength(&quot;   &quot;, 2) == true
	 *     ValidationUtils.hasMaximumLength(&quot;Hi &quot;, 2) == true
	 *     ValidationUtils.hasMaximumLength(&quot;Ewa&quot;, 2) == false
	 * </pre>
	 * 
	 * @param str
	 *            the string to check
	 * @param max
	 *            the maximum length
	 * @return <code>true</code> if the length is less than or equal to the minimum, <code>false</code> otherwise
	 */
	public static boolean hasMaximumLength(String str, int max)
	{
		int length = str == null ? 0 : str.trim().length();
		return length <= max;
	}

	/**
	 * Checks and answers if the length of the given string is in the bounds as specified by the interval [min, max]. Strings that are <code>null</code> or
	 * contain only blanks have length 0.
	 * 
	 * <pre>
	 *     ValidationUtils.hasBoundedLength(null,  1, 2) == false
	 *     ValidationUtils.hasBoundedLength(&quot;&quot;,    1, 2) == false
	 *     ValidationUtils.hasBoundedLength(&quot; &quot;,   1, 2) == false
	 *     ValidationUtils.hasBoundedLength(&quot;   &quot;, 1, 2) == false
	 *     ValidationUtils.hasBoundedLength(&quot;Hi &quot;, 1, 2) == true
	 *     ValidationUtils.hasBoundedLength(&quot;Ewa&quot;, 1, 2) == false
	 * </pre>
	 * 
	 * @param str
	 *            the string to check
	 * @param min
	 *            the minimum length
	 * @param max
	 *            the maximum length
	 * @return <code>true</code> if the length is in the interval, <code>false</code> otherwise
	 * @throws IllegalArgumentException
	 *             if min > max
	 */
	public static boolean hasBoundedLength(String str, int min, int max)
	{
		if (min > max)
			throw new IllegalArgumentException("The minimum length must be less than or equal to the maximum length.");
		int length = str == null ? 0 : str.trim().length();
		return (min <= length) && (length <= max);
	}

	/**
	 * Checks and answers if the given string consists only of digits.
	 * 
	 * <pre>
	 *     ValidationUtils.isDigit(null)  NullPointerException
	 *     ValidationUtils.isDigit(&quot;&quot;)    == true
	 *     ValidationUtils.isDigit(&quot; &quot;)   == false
	 *     ValidationUtils.isDigit(&quot;?&quot;)   == false
	 *     ValidationUtils.isDigit(&quot;   &quot;) == false
	 *     ValidationUtils.isDigit(&quot;12 &quot;) == false
	 *     ValidationUtils.isDigit(&quot;123&quot;) == true
	 *     ValidationUtils.isDigit(&quot;abc&quot;) == false
	 *     ValidationUtils.isDigit(&quot;a23&quot;) == false
	 * </pre>
	 * 
	 * @param str
	 *            the string to check
	 * @return <code>true</code> if the string consists only of digits, <code>false</code> otherwise
	 * @throws NullPointerException
	 *             if the string is <code>null</code>
	 * 
	 * @see Character#isDigit(char)
	 */
	public static boolean isDigit(String str)
	{
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char c = str.charAt(i);
			if (!Character.isDigit(c)) { return false; }
		}
		return true;
	}

	/**
	 * Checks and answers if the given string consists only of letters.
	 * 
	 * <pre>
	 *     ValidationUtils.isLetter(null)  NullPointerException
	 *     ValidationUtils.isLetter(&quot;&quot;)    == true
	 *     ValidationUtils.isLetter(&quot; &quot;)   == false
	 *     ValidationUtils.isLetter(&quot;?&quot;)   == false
	 *     ValidationUtils.isLetter(&quot;   &quot;) == false
	 *     ValidationUtils.isLetter(&quot;12 &quot;) == false
	 *     ValidationUtils.isLetter(&quot;123&quot;) == false
	 *     ValidationUtils.isLetter(&quot;abc&quot;) == true
	 *     ValidationUtils.isLetter(&quot;ab &quot;) == false
	 *     ValidationUtils.isLetter(&quot;a23&quot;) == false
	 * </pre>
	 * 
	 * @param str
	 *            the string to check
	 * @return <code>true</code> if the string consists only of lettes, <code>false</code> otherwise
	 * @throws NullPointerException
	 *             if the string is <code>null</code>
	 * 
	 * @see Character#isLetter(char)
	 */
	public static boolean isLetter(String str)
	{
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char c = str.charAt(i);
			if (!Character.isLetter(c)) { return false; }
		}
		return true;
	}

	/**
	 * Checks and answers if the given string consists only of letters and digits.
	 * 
	 * <pre>
	 *     ValidationUtils.isLetterOrDigit(null)  NullPointerException
	 *     ValidationUtils.isLetterOrDigit(&quot;&quot;)    == true
	 *     ValidationUtils.isLetterOrDigit(&quot; &quot;)   == false
	 *     ValidationUtils.isLetterOrDigit(&quot;?&quot;)   == false
	 *     ValidationUtils.isLetterOrDigit(&quot;   &quot;) == false
	 *     ValidationUtils.isLetterOrDigit(&quot;12 &quot;) == false
	 *     ValidationUtils.isLetterOrDigit(&quot;123&quot;) == true
	 *     ValidationUtils.isLetterOrDigit(&quot;abc&quot;) == true
	 *     ValidationUtils.isLetterOrDigit(&quot;ab &quot;) == false
	 *     ValidationUtils.isLetterOrDigit(&quot;a23&quot;) == true
	 * </pre>
	 * 
	 * @param str
	 *            the string to check
	 * @return <code>true</code> if the string consists only of letters and digits, <code>false</code> otherwise
	 * @throws NullPointerException
	 *             if the string is <code>null</code>
	 * 
	 * @see Character#isDigit(char)
	 * @see Character#isLetter(char)
	 */
	public static boolean isLetterOrDigit(String str)
	{
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char c = str.charAt(i);
			if (!Character.isLetterOrDigit(c)) { return false; }
		}
		return true;
	}

	// Date Checkers *****************************************************

	/**
	 * Determines and answers if the day of the given <code>Date</code> is in the past.
	 * 
	 * @param date
	 *            the date to check
	 * @return <code>true</code> if in the past, <code>false</code> otherwise
	 */
	public static boolean isPastDay(Date date)
	{
		Calendar in = new GregorianCalendar();
		in.setTime(date);
		Calendar today = getRelativeCalendar(0);
		return in.before(today);
	}

	/**
	 * Determines and answers if the given <code>Date</code> is yesterday.
	 * 
	 * @param date
	 *            the date to check
	 * @return <code>true</code> if yesterday, <code>false</code> otherwise
	 */
	public static boolean isYesterday(Date date)
	{
		Calendar in = new GregorianCalendar();
		in.setTime(date);
		Calendar yesterday = getRelativeCalendar(-1);
		Calendar today = getRelativeCalendar(0);
		return !in.before(yesterday) && in.before(today);
	}

	/**
	 * Determines and answers if the given <code>Date</code> is today.
	 * 
	 * @param date
	 *            the date to check
	 * @return <code>true</code> if today, <code>false</code> otherwise
	 */
	public static boolean isToday(Date date)
	{
		GregorianCalendar in = new GregorianCalendar();
		in.setTime(date);
		Calendar today = getRelativeCalendar(0);
		Calendar tomorrow = getRelativeCalendar(+1);
		return !in.before(today) && in.before(tomorrow);
	}

	/**
	 * Determines and answers if the given <code>Date</code> is tomorrow.
	 * 
	 * @param date
	 *            the date to check
	 * @return <code>true</code> if tomorrow, <code>false</code> otherwise
	 */
	public static boolean isTomorrow(Date date)
	{
		GregorianCalendar in = new GregorianCalendar();
		in.setTime(date);
		Calendar tomorrow = getRelativeCalendar(+1);
		Calendar dayAfter = getRelativeCalendar(+2);
		return !in.before(tomorrow) && in.before(dayAfter);
	}

	/**
	 * Determines and answers if the day of the given <code>Date</code> is in the future.
	 * 
	 * @param date
	 *            the date to check
	 * @return <code>true</code> if in the future, <code>false</code> otherwise
	 */
	public static boolean isFutureDay(Date date)
	{
		Calendar in = new GregorianCalendar();
		in.setTime(date);
		Calendar tomorrow = getRelativeCalendar(+1);
		return !in.before(tomorrow);
	}

	/**
	 * Computes the day that has the given offset in days to today and returns it as an instance of <code>Date</code>.
	 * 
	 * @param offsetDays
	 *            the offset in day relative to today
	 * @return the <code>Date</code> that is the begin of the day with the specified offset
	 */
	public static Date getRelativeDate(int offsetDays)
	{
		return getRelativeCalendar(offsetDays).getTime();
	}

	/**
	 * Computes the day that has the given offset in days to today and returns it as an instance of <code>Calendar</code>.
	 * 
	 * @param offsetDays
	 *            the offset in day relative to today
	 * @return a <code>Calendar</code> instance that is the begin of the day with the specified offset
	 */
	public static Calendar getRelativeCalendar(int offsetDays)
	{
		Calendar today = new GregorianCalendar();
		return getRelativeCalendar(today, offsetDays);
	}

	/**
	 * Computes the day that has the given offset in days from the specified <em>from</em> date and returns it as an instance of <code>Calendar</code>.
	 * 
	 * @param from
	 *            the base date as <code>Calendar</code> instance
	 * @param offsetDays
	 *            the offset in day relative to today
	 * @return a <code>Calendar</code> instance that is the begin of the day with the specified offset from the given day
	 */
	public static Calendar getRelativeCalendar(Calendar from, int offsetDays)
	{
		Calendar temp = new GregorianCalendar(from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DATE), 0, 0, 0);
		temp.add(Calendar.DATE, offsetDays);
		return temp;
	}

	public static boolean isZero(int number)
	{
		return number == 0;
	}

	public static boolean isZero(double number)
	{
		return number == 0.0;
	}

	public static boolean isInteger(String number)
	{
		if (number == null)
			return true;
		try
		{
			Integer.parseInt(number);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	public static boolean isDouble(String number)
	{
//		number.replaceAll(",", ".");
		if (number == null)
			return true;
		try
		{
			Double.parseDouble(number);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
}

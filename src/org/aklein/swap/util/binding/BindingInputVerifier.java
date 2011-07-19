package org.aklein.swap.util.binding;

import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

/**
 * InputVerifier for {@link JFormattedTextField}s to verify format while input.<br/>
 * Used with Currency, Integer, Double and Long text fields 
 * 
 * @author AKlein
 * 
 */
public class BindingInputVerifier extends InputVerifier
{
	@Override
	public boolean verify(JComponent input)
	{
		JFormattedTextField ftf = (JFormattedTextField) input;
		JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();

		if (formatter != null)
		{
			String text = ftf.getText();
			try
			{
				formatter.stringToValue(text);
				return true;
			}
			catch (ParseException pe)
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean shouldYieldFocus(JComponent input)
	{
		return verify(input);
	}

}

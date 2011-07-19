package org.aklein.swap.util.validation;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;


import com.jgoodies.validation.view.ValidationComponentUtils;

/**
 * Handler that is notified when the current focus changes<br/>
 * 
 * It handles adding infos with field hints to the InfoQueue  
 * 
 * @author Alexander Klein
 *
 */
public class FocusChangeHandler implements PropertyChangeListener
{
	String key;

	public FocusChangeHandler(String key)
	{
		super();
		this.key = key;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		String propertyName = evt.getPropertyName();
		if (!"permanentFocusOwner".equals(propertyName)) //$NON-NLS-1$
			return;
		if (evt.getNewValue() != null)
		{
			Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			String focusHint = (focusOwner instanceof JComponent) ? (String) ValidationComponentUtils.getInputHint((JComponent) focusOwner) : null;
			QueueRegistry.addInfo(CustomValidator.INFO_TARGET, key, "focusHint", "hint", focusHint); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}

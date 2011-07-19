package org.aklein.swap.ui;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.aklein.swap.util.validation.CustomValidator;
import org.aklein.swap.util.validation.QueueRegistry;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.view.ValidationComponentUtils;

/**
 * Handler that will be notified when any {@link ValidationResult} has been changed, or better to say when any info, warning or error occured or has been removed
 * 
 * @author Alexander Klein
 * 
 * @param <F>
 */
public class ValidationChangeHandler<F extends Container> implements PropertyChangeListener
{
	ViewController<F> controller;

	public ValidationChangeHandler(ViewController<F> controller)
	{
		this.controller = controller;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		// paint mandatory border on mandatory components
		ValidationComponentUtils.updateComponentTreeMandatoryBorder(controller.getContainer());
		// paint background of error/warning/info on components with error/warning/info
		ValidationComponentUtils.updateComponentTreeSeverityBackground(controller.getView().getContainer(), QueueRegistry.getQueue(CustomValidator.ERROR_TARGET, controller));
	}
}

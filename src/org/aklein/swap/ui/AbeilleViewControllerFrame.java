package org.aklein.swap.ui;

import java.io.InputStream;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

/**
 * Implementation of a ViewController based on JFrame using an AbeilleView
 * 
 * @author Alexander Klein
 */
public abstract class AbeilleViewControllerFrame extends ViewControllerFrame<FormPanel, AbeilleView> {

	/**
	 * Instantiate a View from an InputStream
	 * 
	 * @param stream
	 * @throws FormException
	 */
	public AbeilleViewControllerFrame(InputStream stream) throws FormException {
		super();
		view = new AbeilleView(new FormPanel(stream), this);
	}

	/**
	 * Instantiate a View from a file specified by its path
	 * 
	 * @param path
	 */
	public AbeilleViewControllerFrame(String path) {
		super();
		view = new AbeilleView(new FormPanel(path), this);
	}

	/**
	 * Second method to initialize the view, set bindings and attach events.<br/>
	 * Will be called after validations and properties injection.
	 */
	@Override
	protected void afterFix() {
	}

	@Override
	protected void internalInit() {
		initComponents();
		getView().fixMessages();
		afterFix();
	}
}

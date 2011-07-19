package org.aklein.swap.ui;

import java.io.InputStream;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

/**
 * Implementation of a ViewController based on JXPanel using an AbeilleView
 * 
 * @author Alexander Klein
 */
public abstract class AbeilleViewControllerPanel extends ViewControllerPanel<FormPanel, AbeilleView> {
	/**
	 * Instantiate a View from an InputStream
	 * 
	 * @param stream
	 * @throws FormException
	 */
	public AbeilleViewControllerPanel(InputStream stream) throws FormException {
		super();
		view = new AbeilleView(new FormPanel(stream), this);
	}

	/**
	 * Instantiate a View from a file specified by its path
	 * 
	 * @param path
	 */
	public AbeilleViewControllerPanel(String path) {
		super();
		view = new AbeilleView(new FormPanel(path), this);
	}

	/**
	 * Second method to initialize the view, set bindings and attach events.<br/>
	 * Will be called after validations and properties injection.
	 */
	protected void afterFix() {
	}

	@Override
	protected void internalInit() {
		initComponents();
		getView().fixMessages();
		afterFix();
	}
}

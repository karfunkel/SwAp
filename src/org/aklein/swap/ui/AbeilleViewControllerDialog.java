package org.aklein.swap.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.io.InputStream;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

/**
 * Implementation of a ViewController based on JDialog using an AbeilleView
 * 
 * @author Alexander Klein
 */
public abstract class AbeilleViewControllerDialog extends ViewControllerDialog<FormPanel, AbeilleView> {
	/**
	 * Instantiate a View from an InputStream
	 * 
	 * @param stream
	 * @param owner
	 * @throws FormException
	 */
	public AbeilleViewControllerDialog(InputStream stream, Frame owner) throws FormException {
		super(owner);
		view = new AbeilleView(new FormPanel(stream), this);
	}

	/**
	 * Instantiate a View from a file specified by its path
	 * 
	 * @param owner
	 * @param path
	 */
	public AbeilleViewControllerDialog(String path, Frame owner) {
		super(owner);
		view = new AbeilleView(new FormPanel(path), this);
	}

	/**
	 * Instantiate a View from an InputStream
	 * 
	 * @param stream
	 * @param owner
	 * @throws FormException
	 */
	public AbeilleViewControllerDialog(InputStream stream, Dialog owner) throws FormException {
		super(owner);
		view = new AbeilleView(new FormPanel(stream), this);
	}

	/**
	 * Instantiate a View from a file specified by its path
	 * 
	 * @param owner
	 * @param path
	 */
	public AbeilleViewControllerDialog(String path, Dialog owner) {
		super(owner);
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

package org.aklein.swap.ui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.aklein.swap.util.FocusTraversalOnArray;
import org.aklein.swap.util.validation.CustomValidator;
import org.aklein.swap.util.validation.FocusChangeHandler;
import org.aklein.swap.util.validation.QueueRegistry;
import org.apache.commons.configuration.CombinedConfiguration;
import org.jdesktop.application.ActionManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.jeta.forms.gui.common.FormException;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.Validator;

/**
 * Implementation of a ViewController based on JDialog using an AbeilleView
 * 
 * @author Alexander Klein
 */
public abstract class ViewControllerDialog<C extends Container, V extends View<C>> extends JDialog implements ViewController<C> {
	protected V																		view;
	private CustomValidator<ViewControllerDialog<C, V>, ViewControllerDialog<C, V>>	validator;
	private PropertyChangeListener													validationListener;
	private CombinedConfiguration													config;

	protected ViewControllerDialog(Frame owner) {
		super(owner, true);
	}

	protected ViewControllerDialog(Dialog owner) {
		super(owner, true);
	}

	/**
	 * Instantiate a View from an InputStream
	 * 
	 * @param stream
	 * @param owner
	 * @throws FormException
	 */
	public ViewControllerDialog(V view, Frame owner) throws FormException {
		super(owner, true);
		this.view = view;
	}

	/**
	 * Instantiate a View from an InputStream
	 * 
	 * @param stream
	 * @param owner
	 * @throws FormException
	 */
	public ViewControllerDialog(V view, Dialog owner) throws FormException {
		super(owner, true);
		this.view = view;
	}

	/**
	 * @see ViewController#getResourceMap()
	 */
	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(this.getClass());
	}

	/**
	 * @see ViewController#getApplication()
	 */
	public Application getApplication() {
		return Application.getInstance();
	}

	/**
	 * @see ViewController#getContext()
	 */
	public ApplicationContext getContext() {
		return getApplication().getContext();
	}

	/**
	 * @see ViewController#getActionManager()
	 */
	public ActionManager getActionManager() {
		return getContext().getActionManager();
	}

	protected void init(CombinedConfiguration config) {
		setConfig(config);
		init();
	}

	/**
	 * Initializing method, will be run directly after the constructor
	 */
	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		validationListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				getValidator().validate(ViewControllerDialog.this);
			}
		};
		setFocusCycleRoot(true);
		if (focusComponents() != null)
			setFocusTraversalPolicy(new FocusTraversalOnArray(focusComponents()));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		layoutView();
		internalInit();

		Dimension d = getDimension();
		if (d == null) {
			pack();
			d = getSize();
		}
		setBounds(getCenteredBounds(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()), d.width, d.height));

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new FocusChangeHandler(getMessageKey()));
		PropertyChangeListener listener = new ValidationChangeHandler<C>(this);
		QueueRegistry.addPropertyChangeListener(CustomValidator.ERROR_TARGET, ValidationResultModel.PROPERTYNAME_RESULT, listener);
		getValidator().validate(this);
	}

	protected void internalInit() {
		initComponents();
	}

	/**
	 * Method to override default adding behaviour
	 */
	protected void layoutView() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getContainer(), BorderLayout.CENTER);
	}

	/**
	 * If the method 'addPropertyChangeListener' exists on data, the ValidationHandler will be registered to it, so that
	 * with each change of any property, the form will be automatically validated()
	 * 
	 * @param data
	 */
	public void addValidationHandlerTo(Object data) {
		if (data == null)
			return;
		try {
			Method m = data.getClass().getMethod("addPropertyChangeListener", PropertyChangeListener.class);
			m.invoke(data, validationListener);
		}
		catch (SecurityException e) {}
		catch (NoSuchMethodException e) {}
		catch (IllegalArgumentException e) {}
		catch (IllegalAccessException e) {}
		catch (InvocationTargetException e) {}
	}

	/**
	 * If the method 'removePropertyChangeListener' exists on data, the ValidationHandler will be remove from it, so
	 * that automatic validation stops.
	 * 
	 * @param data
	 */
	public void removeValidationHandlerFrom(Object data) {
		if (data == null)
			return;
		try {
			Method m = data.getClass().getMethod("removePropertyChangeListener", PropertyChangeListener.class);
			m.invoke(data, validationListener);
		}
		catch (SecurityException e) {}
		catch (NoSuchMethodException e) {}
		catch (IllegalArgumentException e) {}
		catch (IllegalAccessException e) {}
		catch (InvocationTargetException e) {}
	}

	/**
	 * Returns an Array of Components, that specifies the tab order
	 * 
	 * @return
	 */
	protected abstract Component[] focusComponents();

	/**
	 * Method to initialize the view, set bindings and attach events.<br/>
	 * First validation will occur after the completion of this method.
	 */
	protected abstract void initComponents();

	/**
	 * Second method to initialize the view, set bindings and attach events.<br/>
	 * Will be called after validations and properties injection.
	 */
	protected void afterFix() {
	}

	/**
	 * Returns the initial size of the Dialog
	 * 
	 * @return
	 */
	protected abstract Dimension getDimension();

	/**
	 * Pass through method to the views container
	 * 
	 * @see JComponent#setFocusTraversalKeys(int, Set)
	 */
	@Override
	public void setFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
		if (getContainer() != null)
			getContainer().setFocusTraversalKeys(id, keystrokes);
		super.setFocusTraversalKeys(id, keystrokes);
	}

	/**
	 * Pass through method to the views container
	 * 
	 * @see JComponent#setFocusTraversalKeysEnabled(boolean)
	 */
	@Override
	public void setFocusTraversalKeysEnabled(boolean focusTraversalKeysEnabled) {
		if (getContainer() != null)
			getContainer().setFocusTraversalKeysEnabled(focusTraversalKeysEnabled);
		super.setFocusTraversalKeysEnabled(focusTraversalKeysEnabled);
	}

	/**
	 * Pass through method to the views container
	 * 
	 * @see JComponent#setFocusTraversalPolicy(FocusTraversalPolicy)
	 */
	@Override
	public void setFocusTraversalPolicy(FocusTraversalPolicy policy) {
		if (getContainer() != null)
			getContainer().setFocusTraversalPolicy(policy);
		super.setFocusTraversalPolicy(policy);
	}

	/**
	 * @see ViewController#getAction(String)
	 */
	public Action getAction(String key) {
		return getContext().getActionManager().getActionMap(getClass(), this).get(key);
	}

	/**
	 * @see ViewController#getAction(Object, String)
	 */
	public Action getAction(Object actionsObject, String key) {
		return getContext().getActionManager().getActionMap(actionsObject.getClass(), actionsObject).get(key);
	}

	/**
	 * @see ViewController#getContainer()
	 */
	public C getContainer() {
		if (getView() == null)
			return null;
		return getView().getContainer();
	}

	/**
	 * @see ViewController#getView()
	 */
	public V getView() {
		return view;
	}

	/**
	 * Simplifier method to get the configuration set by spring
	 * 
	 * @return
	 */
	public CombinedConfiguration getConfig() {
		return config;
	}

	/**
	 * Method to set the configuration, normally set by spring
	 * 
	 * @return
	 */
	@Autowired
	public void setConfig(@Qualifier("config") CombinedConfiguration config) {
		this.config = config;
	}

	/**
	 * @see ViewController#getValidator()
	 */
	public Validator<ViewControllerDialog<C, V>> getValidator() {
		if (validator == null)
			validator = new CustomValidator<ViewControllerDialog<C, V>, ViewControllerDialog<C, V>>(this, getMessageKey(), getConfig());
		return validator;
	}

	/**
	 * Method to calculate the bounds centerd on the screen
	 * 
	 * @param srcBounds
	 * @param width
	 * @param height
	 * @return
	 */
	public static Rectangle getCenteredBounds(Rectangle srcBounds, int width, int height) {
		int x = 0;
		int y = 0;
		if (srcBounds != null) {
			x = (int) Math.round(srcBounds.getCenterX() - (width / 2));
			y = (int) Math.round(srcBounds.getCenterY() - (height / 2));
		}
		return new Rectangle(x, y, width, height);
	}

}

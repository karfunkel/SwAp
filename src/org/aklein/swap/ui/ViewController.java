package org.aklein.swap.ui;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Action;

import org.aklein.swap.util.MessageKeyProvider;
import org.jdesktop.application.ActionManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.jgoodies.validation.Validator;

/**
 * The Controller in the MVC concept.<br/>
 * 
 * Any calculations, bindings and logic should reside in here.
 * 
 * @author Alexander Klein
 * 
 * @param <D>
 * @param <F>
 */
public interface ViewController<F extends Container> extends MessageKeyProvider
{
	/**
	 * Returns the container from the view
	 * 
	 * @return
	 */
	public F getContainer();

	/**
	 * Reference to the view
	 * 
	 * @return
	 */
	public View<F> getView();

	/**
	 * Returns the Validator for this ViewController.<br/>
	 * 
	 * This should be able to validate the whole view.
	 * 
	 * @return
	 */
	public Validator<? extends Component> getValidator();

	/**
	 * Returns the {@link ResourceMap} for this class.
	 * 
	 * @return
	 */
	public ResourceMap getResourceMap();

	/**
	 * Returns the current {@link Application}
	 * 
	 * @return
	 */
	public Application getApplication();

	/**
	 * Returns the applications {@link ActionManager}
	 * 
	 * @return
	 */
	public ActionManager getActionManager();

	/**
	 * Returns the {@link Action} on the current object with the given name
	 * 
	 * @return
	 */
	public Action getAction(String name);

	/**
	 * Returns the {@link Action} on the given object with the given name
	 * 
	 * @return
	 */
	public Action getAction(Object actionsObject, String name);

	/**
	 * Returns the applications {@link ApplicationContext}.
	 * 
	 * @return
	 */
	public ApplicationContext getContext();
}

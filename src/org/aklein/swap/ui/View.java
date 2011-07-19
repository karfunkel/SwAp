package org.aklein.swap.ui;

import java.awt.Container;
import java.util.Map;

/**
 * The View in the MVC concept.<br/>
 * It should only contain code for display, no events, calculation or other code. This is done in the Controller.
 * 
 * @author Alexander Klein
 * @param <D> DataSource
 * @param <F> Container
 */
public interface View<F extends Container> {
	/**
	 * Reference to the ViewController of this View
	 * 
	 * @return
	 */
	public ViewController<F> getController();

	/**
	 * generic getter to get a component or value from the view. The result might be a View itself.
	 * 
	 * @param name
	 * @return
	 */
	public Object getProperty(String name);

	/**
	 * generic setter to set a component or value to the view.
	 * 
	 * @param name
	 * @return
	 */
	public void setProperty(String name, Object value);

	/**
	 * A Map of dynamic properties NOT defined in the view, but to add dynamic values.<br/>
	 * Should be used in get/setProperty aswell.
	 * 
	 * @return
	 */
	public Map<String, Object> getProperties();

	/**
	 * Returns the Container wrapped by the View. This might be a JPanel or the like.
	 * 
	 * @return
	 */
	public F getContainer();

}

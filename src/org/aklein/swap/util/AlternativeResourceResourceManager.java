/**
 * 
 */
package org.aklein.swap.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jdesktop.application.RecursiveGenericResourceMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.jdesktop.application.ResourceMap;

/**
 * Extends the base implementation in that for every bundle that
 * should be contained in a {@code ResourceMap} it adds another 
 * bundle. This second bundle can be used to overwrite and add 
 * settings in/to the default bundle.   
 *   
 * @see ResourceManager
 * @author fadingsun
 */
public class AlternativeResourceResourceManager extends ResourceManager {
	private static String alternativeBundleSuffix = "_custom";
	
	/**
     * Construct a {@code AlternativeResourceResourceManager}.  Typically applications
     * will create a AlternativeResourceResourceManager directly only once to replace 
     * the automatically created {@code ResourceManager} and from then on , 
     * they'll retrieve the shared one from the {@code ApplicationContext} with:
     * <pre>
     * Application.getInstance().getContext().getResourceManager()
     * </pre>
     * Or just look up {@code ResourceMaps} with the ApplicationContext
     * convenience method:
     * <pre>
     * Application.getInstance().getContext().getResourceMap(MyClass.class)
     * </pre>
     * 
     * @param context the context this {@link AlternativeResourceResourceManager} belongs to, 
     *                for some convenience methods to work
     * @see ApplicationContext#getResourceManager
     * @see ApplicationContext#getResourceMap
     */
	public AlternativeResourceResourceManager(ApplicationContext context) {
		super(context);
	}
	
	
	/**
	 * Constructor with added parameter to overwrite the suffix used for
	 * identifying the alternative bundle.
	 * 
	 * @param context
	 * @param alternativeBundle
	 * @see AlternativeResourceResourceManager#AlternativeResourceResourceManager(ApplicationContext)
	 */
	public AlternativeResourceResourceManager(ApplicationContext context, String alternativeBundle) {
		super(context);
		alternativeBundleSuffix = alternativeBundle;
	}
	/**
     * Called by {@link #getResourceMap} to construct {@code ResourceMaps}.
     * 
	 * This method adds the {@code ResourceBundle} Name that may hold customizations for 
	 * every entry in the bundleNames list to this list 
     */
    protected ResourceMap createResourceMap(ClassLoader classLoader, ResourceMap parent, List<String> bundleNames) {
    	List<String> modifiedBundleNames = new ArrayList<String>();
    	String currentBundleName;
    	for(ListIterator<String> lI = bundleNames.listIterator(); lI.hasNext();) {
    		currentBundleName = lI.next();
    		modifiedBundleNames.add(currentBundleName+alternativeBundleSuffix);
    		modifiedBundleNames.add(currentBundleName);    		
    	}
	return new RecursiveGenericResourceMap(parent, classLoader, modifiedBundleNames);
    }
}

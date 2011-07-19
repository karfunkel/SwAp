package org.aklein.swap.ui;

import java.awt.Component;
import java.awt.Container;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.ResourceMap;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;
import com.jeta.open.gui.framework.JETAContainer;
import com.jeta.open.gui.framework.UIDirector;
import com.jeta.open.registry.JETARegistry;
import com.jeta.open.resources.AppResourceLoader;
import com.jeta.open.resources.ResourceLoader;
import com.jeta.open.support.ComponentFinder;
import com.jeta.open.support.ComponentFinderFactory;
import com.jeta.open.support.HierarchicalComponentFinderFactory;

public class AbeilleView extends AbstractView<FormPanel> {
    protected FormPanel container;

    private Map<String, WeakReference<Component>> m_components = null;
    static {
        JETARegistry.rebind(ResourceLoader.COMPONENT_ID, new AppResourceLoader());
        JETARegistry.rebind(ComponentFinderFactory.COMPONENT_ID, new HierarchicalComponentFinderFactory());
    }

    /**
     * Instantiate a View from an InputStream
     * @param stream
     * @param controller
     * @throws FormException
     */
    public AbeilleView(InputStream stream, ViewController<FormPanel> controller) throws FormException {
        this(new FormPanel(stream), controller);
    }

    /**
     * Instantiate a View from a file specified by its path
     * @param path
     * @param controller
     */
    public AbeilleView(String path, ViewController<FormPanel> controller) {
        this(new FormPanel(path), controller);
    }

    /**
     * Instantiate a View with a FormPanel and the controller
     * @param container
     * @param controller
     */
    public AbeilleView(FormPanel container, ViewController<FormPanel> controller) {
        super(controller);
        this.container = container;
        this.container.setOpaque(false);
    }

    @Override
    protected View<FormPanel> createView(FormPanel comp, ViewController<FormPanel> controller) {
        return new AbeilleView(comp, controller);
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // TODO Auto-generated method stub

    }

    public FormPanel getContainer() {
        if (container instanceof FormPanel)
            return container;
        return null;
    }

    public void fixMessages() {
        ResourceMap resource = getController().getResourceMap();
        resource.injectComponents((Component) getController());
        getContainer().revalidate();
        getContainer().repaint();
    }

    /*
     * (non-Javadoc)
     * @see org.aklein.swap.ui.AbstractView#getComponentByName(java.awt.Container, java.lang.String)
     */
    @Override
    protected Component getComponentByName(Container container, String name) {
        if (name.startsWith("."))
            name = name.substring(1);
        Component result = null;
        if (container instanceof JETAContainer)
            result = ((JETAContainer) container).getComponentByName(name);
        if (result == null)
            result = super.getComponentByName(container, name);
        return result;
    }

    public Collection getComponentsByName(String name) {
        if (name.startsWith("."))
            name = name.substring(1);
        return container.getComponentsByName(name);
    }

    public Collection getAllNamedComponents() {
        if (m_components == null) {
            m_components = new HashMap<String, WeakReference<Component>>();
            buildNames(container);
        }

        LinkedList<Component> components = new LinkedList<Component>();

        Iterator names = m_components.keySet().iterator();
        while (names.hasNext()) {
            String name = (String) names.next();
            WeakReference<Component> wref = m_components.get(name);
            if (wref != null)
                components.addLast(wref.get());
        }

        return components;
    }

    /**
     * Recursively searches all Components owned by this container. If the Component has a name, we store it in the m_components hash table
     * @param container the container to search
     */
    protected void buildNames(Container container) {
        if (container != null) {
            if (container instanceof JMenu) {
                buildNames(((JMenu) container).getPopupMenu());
            } else {
                registerComponent(container);
                int count = container.getComponentCount();
                for (int index = 0; index < count; index++) {
                    Component comp = container.getComponent(index);
                    if (comp instanceof Container)
                        buildNames((Container) comp);
                    else {
                        registerComponent(comp);
                    }
                }
            }
        } else {
            assert (false);
        }
    }

    private void registerComponent(Component comp) {
        if (comp == null)
            return;

        String name = comp.getName();
        if (name != null && name.length() > 0) {
            m_components.put(name, new WeakReference<Component>(comp));
        }
    }

    public void reset() {
        if (container instanceof ComponentFinder)
            ((ComponentFinder) container).reset();
    }

    public void enableComponent(String commandId, boolean bEnable) {
        container.enableComponent(commandId, bEnable);
    }

    public UIDirector getUIDirector() {
        return container.getUIDirector();
    }

    public static String getText(Component comp) {
        if (comp instanceof JTextComponent) {
            return ((JTextComponent) comp).getText();
        } else if (comp instanceof AbstractButton) {
            return ((AbstractButton) comp).getText();
        } else if (comp instanceof JLabel) {
            return ((JLabel) comp).getText();
        } else {
            try {
                if (comp != null) {
                    Class<? extends Component> c = comp.getClass();
                    Class[] params = new Class[0];
                    Object[] values = new Object[0];
                    java.lang.reflect.Method m = c.getDeclaredMethod("getText", params);
                    Object obj = m.invoke(comp, values);
                    return obj == null ? null : obj.toString();
                }
            } catch (Exception e) {
                // ignore
            }
            return null;
        }
    }

    public static void setText(Component comp, String txt) {
        if (comp instanceof JTextComponent) {
            ((JTextComponent) comp).setText(txt);
        } else if (comp instanceof JLabel) {
            ((JLabel) comp).setText(txt);
        } else if (comp instanceof AbstractButton) {
            ((AbstractButton) comp).setText(txt);
        } else {
            try {
                if (comp != null) {
                    Class<? extends Component> c = comp.getClass();
                    Class[] params = new Class[] { String.class };
                    Object[] values = new Object[] { txt };
                    java.lang.reflect.Method m = c.getDeclaredMethod("setText", params);
                    m.invoke(comp, values);
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

}

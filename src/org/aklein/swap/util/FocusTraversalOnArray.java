package org.aklein.swap.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;

/**
 * Focus traversal policy based on array of components.
 * 
 */
public class FocusTraversalOnArray extends FocusTraversalPolicy {
    private final Component components[];

    /**
     * Create the focus traversal policy
     * @param components
     */
    public FocusTraversalOnArray(Component components[]) {
	this.components = components;
    }

    private int indexCycle(int index, int delta) {
        int size = this.components.length;
        if (size == 0)
            return index;
        int next = ((index + delta) + size) % size;
        return next;
    }

    private Component cycle(Component currentComponent, int delta) {
        int index = -1;
        for (int i = 0; i < this.components.length; i++) {
            Component component = this.components[i];
            if (component == currentComponent) {
                index = i;
                break;
            }
        }
        // try to find enabled component in "delta" direction
        int initialIndex = index;
        while (true) {
            int newIndex = indexCycle(index, delta);
            if (newIndex == initialIndex) {
                break;
            }
            index = newIndex;
            //
            Component component = this.components[newIndex];
            if (component != null && component.isEnabled() && component.isVisible()) {
                return component;
            }
        }
        // not found
        return currentComponent;
    }

    @Override
    public Component getComponentAfter(Container container, Component component) {
        return cycle(component, 1);
    }

    @Override
    public Component getComponentBefore(Container container, Component component) {
        return cycle(component, -1);
    }

    @Override
    public Component getFirstComponent(Container container) {
        if (this.components != null && this.components.length > 0)
            return this.components[0];
        else
            return null;
    }

    @Override
    public Component getLastComponent(Container container) {
    if (this.components == null || this.components.length < 1)
        return null;
    return this.components[this.components.length - 1];
    }

    @Override
        public Component getDefaultComponent(Container container) {
    return getFirstComponent(container);
    }

    public Component getInitialComponent(Window window) {
        return getFirstComponent(window);    
    }
}
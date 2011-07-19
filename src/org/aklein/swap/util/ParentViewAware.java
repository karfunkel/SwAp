package org.aklein.swap.util;

import java.awt.Container;

import org.aklein.swap.ui.ViewController;

public interface ParentViewAware<T extends ViewController<? extends Container>> {

	public void setParentPanel(T parent);

	public T getParentPanel();

}

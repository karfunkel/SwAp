package org.aklein.swap.examples.basic;

import java.awt.Component;

import org.aklein.swap.ui.AbeilleViewControllerPanel;

import com.jeta.forms.gui.common.FormException;

public class DemoPanel extends AbeilleViewControllerPanel {
	private static final long	serialVersionUID	= -873021613629903607L;

	public DemoPanel() throws FormException {
		super(DemoPanel.class.getResourceAsStream("resources/Demo.jfrm"));
	}

	@Override
	protected Component[] focusComponents() {
		return new Component[] { getHeader(), getDate(), getSlider(), getMonth() };
	}

	@Override
	protected void initComponents() {
	}

	public String getMessageKey() {
		return "Demo";
	}

	// Generation 'Demo' START
	public org.jdesktop.swingx.JXHeader getHeader() {
		return (org.jdesktop.swingx.JXHeader) getView().getProperty("header");
	}

	public org.jdesktop.swingx.JXDatePicker getDate() {
		return (org.jdesktop.swingx.JXDatePicker) getView().getProperty("date");
	}

	public com.jeta.forms.components.label.JETALabel getDateLbl() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("dateLbl");
	}

	public org.jdesktop.swingx.JXMonthView getMonth() {
		return (org.jdesktop.swingx.JXMonthView) getView().getProperty("month");
	}

	public com.jeta.forms.components.label.JETALabel getMonthLbl() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("monthLbl");
	}

	public javax.swing.JSlider getSlider() {
		return (javax.swing.JSlider) getView().getProperty("slider");
	}

	public com.jeta.forms.components.label.JETALabel getSliderLbl() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("sliderLbl");
	}
	// Generation 'Demo' END
}

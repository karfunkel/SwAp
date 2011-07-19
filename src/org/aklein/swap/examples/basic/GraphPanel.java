package org.aklein.swap.examples.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.aklein.swap.ui.AbeilleViewControllerPanel;
import org.jdesktop.application.Action;
import org.jdesktop.swingx.JXGraph;

import com.jeta.forms.gui.common.FormException;

public class GraphPanel extends AbeilleViewControllerPanel {
	private static final long	serialVersionUID	= -7427914045719341367L;
	private int					zoomValue;

	public GraphPanel() throws FormException {
		super(GraphPanel.class.getResourceAsStream("resources/Graph.jfrm"));
	}

	@Override
	protected Component[] focusComponents() {
		return new Component[] { getGraph(), getZoomIn(), getZoomOut() };
	}

	public String getMessageKey() {
		return "Graph";
	}

	@Override
	protected void initComponents() {
		getGraph().addPlots(Color.RED, new JXGraph.Plot() {
			@Override
			public double compute(double value) {
				return Math.cos(value * 3.0d);
			}
		});
		getGraph().addPlots(Color.GREEN, new JXGraph.Plot() {
			@Override
			public double compute(double value) {
				return Math.sin(value * 3.0d);
			}
		});
		getZoomIn().putClientProperty("substancelaf.buttonnominsize", Boolean.TRUE);
		getZoomIn().setAction(getContext().getActionMap(this).get("doZoomIn"));
		getZoomOut().putClientProperty("substancelaf.buttonnominsize", Boolean.TRUE);
		getZoomOut().setAction(getContext().getActionMap(this).get("doZoomOut"));
	}

	@Action
	public void doZoomIn(ActionEvent e) {
		zoom(-5);
	}

	@Action
	public void doZoomOut(ActionEvent e) {
		zoom(5);
	}

	private void zoom(int value) {
		MouseWheelListener[] listeners = getGraph().getMouseWheelListeners();
		for (MouseWheelListener listener : listeners) {
			listener.mouseWheelMoved(new MouseWheelEvent(getGraph(), 1000, System.currentTimeMillis(), 0, 0, 0, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, value));
		}
	}

	public int getZoomValue() {
		return zoomValue;
	}

	public void setZoomValue(int zoomValue) {
		Object old = this.zoomValue;
		this.zoomValue = zoomValue;
		firePropertyChange("zoomValue", old, zoomValue);
	}

	// Generation 'Graph' START
	public org.jdesktop.swingx.JXGraph getGraph() {
		return (org.jdesktop.swingx.JXGraph) getView().getProperty("graph");
	}

	public javax.swing.JButton getZoomIn() {
		return (javax.swing.JButton) getView().getProperty("zoomIn");
	}

	public javax.swing.JButton getZoomOut() {
		return (javax.swing.JButton) getView().getProperty("zoomOut");
	}
	// Generation 'Graph' END
}

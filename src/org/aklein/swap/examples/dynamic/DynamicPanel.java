package org.aklein.swap.examples.dynamic;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import javax.swing.JButton;

import org.aklein.swap.ui.AbeilleViewControllerPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jdesktop.application.Action;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ShadingColorHighlighter;

import com.jeta.forms.gui.common.FormException;

public class DynamicPanel extends AbeilleViewControllerPanel {
	private static final long	serialVersionUID	= -9188137121692186025L;
	private static Log			log					= LogFactory.getLog(DynamicPanel.class);

	public DynamicPanel() throws FormException {
		super(DynamicPanel.class.getResourceAsStream("resources/GTS.jfrm"));
	}

	@Override
	protected Component[] focusComponents() {
		return new Component[] { getActionBar(), getGrid() };
	}

	@Override
	protected void initComponents() {
		fillActions("/item");
		try {
			getGrid().setColumnControlVisible(true);
			getGrid().setRolloverEnabled(true);
			getGrid().setShowGrid(true, false);
			getGrid().setSortable(true);
			getGrid().setAutoCreateColumnsFromModel(false);
			getGrid().setHighlighters(new ShadingColorHighlighter(new HighlightPredicate() {
				public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
					return (adapter.row % 2) == 1;
				}
			}));
			final XPathTableModel model = new XPathTableModel(new File("guided_diagnostics.xml").toURI().toURL(), "/item", "item", "action");
			getGrid().setColumnModel(model);
			getGrid().setModel(model);

			getGrid().addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = getGrid().getSelectedRow();
					row = getGrid().convertRowIndexToModel(row);
					Element data = model.getRow(row);
					model.setRoot(data.getUniquePath());
					fillActions(data.getUniquePath());
				}
			});
		}
		catch (MalformedURLException e) {
			log.error(e);
		}
		catch (DocumentException e) {
			log.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void fillActions(String root) {
		getActionBar().removeAll();
		getActionBar().add(new JButton(getContext().getActionMap(this).get("refresh")));
		getActionBar().add(new JButton(getContext().getActionMap(this).get("startOver")));

		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new File("guided_diagnostics.xml"));
			List<Element> elems = doc.selectNodes(root + "/*");
			for (Element elem : elems)
				if (elem.getName().equals("action"))
					getActionBar().add(new JButton(elem.getTextTrim()));
			getActionBar().revalidate();
			getActionBar().repaint();
		}
		catch (DocumentException e) {
			log.error(e);
		}
	}

	public String getMessageKey() {
		return "DynamicPanel";
	}

	@Action(name = "refresh")
	public void doRefresh() {
		((XPathTableModel) getGrid().getModel()).reload();
		doStartOver();
	}

	@Action(name = "startOver")
	public void doStartOver() {
		fillActions("/item");
		((XPathTableModel) getGrid().getModel()).setRoot("/item");
	}

	// Generation 'GTS' START
	public javax.swing.JToolBar getActionBar() {
		return (javax.swing.JToolBar) getView().getProperty("actionBar");
	}

	public org.jdesktop.swingx.JXTable getGrid() {
		return (org.jdesktop.swingx.JXTable) getView().getProperty("grid");
	}
	// Generation 'GTS' END

}

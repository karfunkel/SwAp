package org.aklein.swap.examples.basic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableColumnModel;

import org.aklein.swap.ui.AbeilleView;
import org.aklein.swap.ui.AbeilleViewControllerPanel;
import org.aklein.swap.ui.View;
import org.aklein.swap.ui.ViewControllerPanel;
import org.aklein.swap.util.TableUtil;
import org.aklein.swap.util.binding.SwingBinder;
import org.aklein.swap.util.validation.CustomValidator;
import org.aklein.swap.util.validation.Queue;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ShadingColorHighlighter;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.SelectionInList;

public class GuiTestPanel extends AbeilleViewControllerPanel {
    private static final long	serialVersionUID	= 6339635323633103996L;
    private Data				data;
    private Locale				locale				= Locale.getDefault();
    private static Log			log					= LogFactory.getLog(GuiTestPanel.class);
    private DataTableModel		model;
    private TableColumnModel	columnModel;
    private String[]			laf;
    private List<String[]>		themes;
    @Autowired
    @Qualifier("GraphPanel")
    private GraphPanel			graphPanel;
    @Autowired
    @Qualifier("DemoPanel")
    private DemoPanel			demoPanel;
    @Autowired
    @Qualifier("errorQueue")
    private Queue				errorQueue;
    @Autowired
    @Qualifier("infoQueue")
    private Queue				infoQueue;

    private static Locale		ROOT				= Locale.getDefault();

    public GuiTestPanel() throws FormException {
        super(GuiTestPanel.class.getResourceAsStream("resources/GuiTest.jfrm"));//$NON-NLS-1$
    }

    @Override
    protected Component[] focusComponents() {
        return new Component[] { getVorname(), getNachname(), getStrasse(), getPlz(), getOrt(), getTel(), getFax(), getAnzeigeButton(), getAddButton(), getAdressen() };
    }

    public String getMessageKey() {
        return "GuiTestPanel";
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        Object old = this.locale;
        this.locale = locale;
        firePropertyChange("locale", old, locale);
    }

    public List<String[]> getThemes() {
        if (themes == null) {
            HierarchicalConfiguration sub = getConfig().configurationAt("theme.available");
            themes = new ArrayList<String[]>();
            for (Iterator iterator = sub.getKeys(); iterator.hasNext();) {
                String[] v = new String[2];
                v[0] = (String) iterator.next();
                v[1] = sub.getString(v[0]);
                themes.add(v);
            }
        }
        return themes;
    }

    public String[] findTheme(String cls) {
        for (String[] theme : getThemes()) {
            if (theme[1].equals(cls))
                return theme;
        }
        return null;
    }

    @Override
    protected void initComponents() {
        getToolbar().add(getContext().getActionMap(this).get("showDemo"));
        getToolbar().add(getContext().getActionMap(this).get("showGraph"));

        getPlaceholder().setLayout(new BorderLayout());
        showDemo(null);

        data = new Data();
        bindData(data);

        columnModel = new DefaultTableColumnModelExt();
        TableUtil.fillTableColumns(getConfig(), columnModel, "adressen");
        getAdressen().setAutoCreateRowSorter(false);
        getAdressen().setRowSorter(null);
        getAdressen().setColumnModel(columnModel);
        model = new DataTableModel(columnModel);
        model.add(new Data("Hans", "Müller", "Testweg 1", "12345", "Testingen", "", ""));
        model.add(new Data("Gisela", "Schmidt", "Testweg 1", "54321", "Musterstadt", "", ""));
        getAdressen().setModel(model);
        getAdressen().setAutoCreateRowSorter(true);
        getAdressen().setColumnControlVisible(true);
        getAdressen().setRolloverEnabled(true);
        getAdressen().setShowGrid(true, false);
        getAdressen().setSortable(true);
        getAdressen().setHighlighters(new ShadingColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                return (adapter.row % 2) == 1;
            }
        }));

        getAdressen().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int row = getAdressen().getSelectedRow();
                    row = getAdressen().convertRowIndexToModel(row);
                    // Data data = model.getRow(row);
                    doAnzeigeButton(null);
                }
            }
        });

        List<Locale> locales = new ArrayList<Locale>();
        locales.add(null);
        locales.add(Locale.GERMANY);

        SwingBinder.getSharedInstance().bindComboBox(getSprache(), new SelectionInList<Locale>(locales, new PropertyAdapter<GuiTestPanel>(this, "locale", true)), new DefaultListCellRenderer() {
            private static final long	serialVersionUID	= -3324480813395748208L;

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel r = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null)
                    r.setText(getResourceMap().getString("locale.default"));
                else if (value instanceof Locale)
                    r.setText(((Locale) value).getDisplayName());
                return r;
            }
        }, getValidationListener());

        setLocale(null);

        addPropertyChangeListener("locale", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() == null) {
                    Locale.setDefault(ROOT);
                    JComponent.setDefaultLocale(ROOT);
                }
                else if (evt.getNewValue() instanceof Locale) {
                    Locale.setDefault((Locale) evt.getNewValue());
                    JComponent.setDefaultLocale((Locale) evt.getNewValue());
                }

                try {
                    for (int i = 0; i < getConfig().getNumberOfConfigurations(); i++) {
                        Configuration c = getConfig().getConfiguration(i);
                        if (c instanceof FileConfiguration) {
                            FileConfiguration conf = (FileConfiguration) c;
                            conf.reload();
                        }
                    }
                }
                catch (Exception e) {
                    log.error("Could not reload Configuration", e);
                }
                reloadQueues(getMessageKey());
                TableUtil.reloadTableColumnModel(getConfig(), columnModel, "adressen");
                ((CustomValidator<ViewControllerPanel<FormPanel, AbeilleView>, ViewControllerPanel<? extends Container, View<? extends Container>>>) getValidator()).reloadRuleSets();
                getValidator().validate(GuiTestPanel.this);
                (GuiTestPanel.this.getView()).fixMessages();
                (getGraphPanel().getView()).fixMessages();
                (getDemoPanel().getView()).fixMessages();
            }
        });

        SwingBinder.getSharedInstance().bindComboBox(getTheme(), new SelectionInList<String[]>(getThemes(), new PropertyAdapter<GuiTestPanel>(this, "laf", true)), new DefaultListCellRenderer() {
            private static final long	serialVersionUID	= -5793665565483118518L;

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null)
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String[] v = (String[]) value;
                JLabel r = (JLabel) super.getListCellRendererComponent(list, v[0], index, isSelected, cellHasFocus);
                r.setToolTipText(v[1]);
                return r;
            }
        }, getValidationListener());

        setLaf(findTheme(getConfig().getString("theme.lookandfeel")));

        addPropertyChangeListener("laf", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    final String[] v = (String[]) evt.getNewValue();
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                UIManager.setLookAndFeel(v[1]);
                                SwingXUtilities.updateAllComponentTreeUIs();
                            }
                            catch (Exception e) {
                                log.warn("Could not change Look and Feel", e);
                            }
                        }
                    });
                }
            }
        });

        addValidationHandlerTo(data);

        Queue.bindLabel(infoQueue, getInfos());
        Queue.bindLabel(errorQueue, getErrors());

        getAddButton().setAction(getContext().getActionMap(this).get("doAddButton"));
        getAnzeigeButton().setAction(getContext().getActionMap(this).get("doAnzeigeButton"));
    }

    public void reloadQueues(String scope) {
        ResourceMap resource = getContext().getResourceMap();
        errorQueue.setDescription(resource.getString("Queue.errors"));
        infoQueue.setDescription(resource.getString("Queue.infos"));
        errorQueue.clearMessages();
        errorQueue.refresh(scope);
        infoQueue.clearMessages();
        infoQueue.refresh(scope);
    }

    private void bindData(Data data) {
        SwingBinder.getSharedInstance().bindFormattedTextField(getVorname(), new PropertyAdapter<Data>(data, "vorname", true), (Format) null, getValidationListener());
        SwingBinder.getSharedInstance().bindFormattedTextField(getNachname(), new PropertyAdapter<Data>(data, "nachname", true), (Format) null, getValidationListener());
        SwingBinder.getSharedInstance().bindFormattedTextField(getStrasse(), new PropertyAdapter<Data>(data, "strasse", true), (Format) null, getValidationListener());
        SwingBinder.getSharedInstance().bindFormattedTextField(getPlz(), new PropertyAdapter<Data>(data, "plz", true), (Format) null, getValidationListener());
        SwingBinder.getSharedInstance().bindFormattedTextField(getOrt(), new PropertyAdapter<Data>(data, "ort", true), (Format) null, getValidationListener());
        SwingBinder.getSharedInstance().bindFormattedTextField(getTel(), new PropertyAdapter<Data>(data, "tel", true), (Format) null, getValidationListener());
        SwingBinder.getSharedInstance().bindFormattedTextField(getFax(), new PropertyAdapter<Data>(data, "fax", true), (Format) null, getValidationListener());
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public DemoPanel getDemoPanel() {
        return demoPanel;
    }

    @Action
    public void showGraph(ActionEvent e) {
        getPlaceholder().removeAll();
        getPlaceholder().add(getGraphPanel(), BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    @Action
    public void showDemo(ActionEvent e) {
        getPlaceholder().removeAll();
        getPlaceholder().add(getDemoPanel(), BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    @Action
    public void doAddButton(final ActionEvent e) {
        if (errorQueue.hasErrors()) {
            JOptionPane.showMessageDialog(this, getResourceMap().getString("errors_exist.text", errorQueue.getResult().size()), getResourceMap().getString("errors_exist.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        else {
            model.add(data);
            data = new Data();
            bindData(data);
        }
    }

    @Action
    public void doAnzeigeButton(final ActionEvent e) {
        int row = getAdressen().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, getResourceMap().getString("please_select.text"), getResourceMap().getString("please_select.title"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        row = getAdressen().convertRowIndexToModel(row);
        Data data = model.getRow(row);
        JOptionPane.showMessageDialog(this, getResourceMap().getString("info.text", data.getVorname(), data.getNachname(), data.getStrasse(), data.getPlz(), data.getOrt(), data.getTel(),
                data.getFax()), getResourceMap().getString("info.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    public String[] getLaf() {
        return laf;
    }

    public void setLaf(String[] laf) {
        Object old = this.laf;
        this.laf = laf;
        firePropertyChange("laf", old, laf);
    }

    // Generation 'GuiTest' START
    public com.jeta.forms.components.label.JETALabel getVornameLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("vornameLbl");
    }

    public com.jeta.forms.components.label.JETALabel getNachnameLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("nachnameLbl");
    }

    public com.jeta.forms.components.label.JETALabel getStrasseLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("strasseLbl");
    }

    public com.jeta.forms.components.label.JETALabel getPlzLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("plzLbl");
    }

    public com.jeta.forms.components.label.JETALabel getOrtLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("ortLbl");
    }

    public com.jeta.forms.components.label.JETALabel getFaxLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("faxLbl");
    }

    public com.jeta.forms.components.label.JETALabel getTelefonLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("telefonLbl");
    }

    public com.jeta.forms.components.label.JETALabel getHeader() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("header");
    }

    public javax.swing.JFormattedTextField getStrasse() {
        return (javax.swing.JFormattedTextField) getView().getProperty("strasse");
    }

    public javax.swing.JFormattedTextField getTel() {
        return (javax.swing.JFormattedTextField) getView().getProperty("tel");
    }

    public javax.swing.JFormattedTextField getPlz() {
        return (javax.swing.JFormattedTextField) getView().getProperty("plz");
    }

    public javax.swing.JFormattedTextField getVorname() {
        return (javax.swing.JFormattedTextField) getView().getProperty("vorname");
    }

    public com.jeta.forms.components.label.JETALabel getErrors() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("errors");
    }

    public com.jeta.forms.components.label.JETALabel getSpracheLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("spracheLbl");
    }

    public javax.swing.JComboBox getSprache() {
        return (javax.swing.JComboBox) getView().getProperty("sprache");
    }

    public javax.swing.JFormattedTextField getNachname() {
        return (javax.swing.JFormattedTextField) getView().getProperty("nachname");
    }

    public javax.swing.JFormattedTextField getOrt() {
        return (javax.swing.JFormattedTextField) getView().getProperty("ort");
    }

    public javax.swing.JFormattedTextField getFax() {
        return (javax.swing.JFormattedTextField) getView().getProperty("fax");
    }

    public com.jeta.forms.components.label.JETALabel getInfos() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("infos");
    }

    public com.jeta.forms.components.label.JETALabel getThemeLbl() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("themeLbl");
    }

    public javax.swing.JComboBox getTheme() {
        return (javax.swing.JComboBox) getView().getProperty("theme");
    }

    public javax.swing.JButton getAnzeigeButton() {
        return (javax.swing.JButton) getView().getProperty("anzeigeButton");
    }

    public javax.swing.JButton getAddButton() {
        return (javax.swing.JButton) getView().getProperty("addButton");
    }

    public org.jdesktop.swingx.JXTable getAdressen() {
        return (org.jdesktop.swingx.JXTable) getView().getProperty("adressen");
    }

    public com.jeta.forms.components.label.JETALabel getPlaceholder() {
        return (com.jeta.forms.components.label.JETALabel) getView().getProperty("placeholder");
    }

    public javax.swing.JToolBar getToolbar() {
        return (javax.swing.JToolBar) getView().getProperty("toolbar");
    }
    // Generation 'GuiTest' END

}

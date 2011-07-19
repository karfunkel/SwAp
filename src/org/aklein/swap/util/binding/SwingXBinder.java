package org.aklein.swap.util.binding;

import java.beans.PropertyChangeListener;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnExtWrapper;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;

/**
 * methods that bind and mend frequently used Swing components using a given
 * ValueModel.
 * @author ka82pl
 */
public class SwingXBinder extends SwingBinder {
    private static SwingXBinder sharedInstance = new SwingXBinder();

    /**
     * getter for shared instance.
     * @return the shared instance
     */
    public static SwingXBinder getSharedInstance() {
        return sharedInstance;
    }

    /**
     * Binds a JXTable to a given ValueModel.
     * @param <T> - type
     * @param table - table to bind to
     * @param columnModel - column model of the table
     * @param listModel - list (row) model auf the table
     * @param selectionValueModel - model of the selection value
     * @param validationListener - [optional] list of validation listeners
     */
    public <T>void bindJXTable(final JXTable table, TableColumnModel columnModel, ListModel listModel, ValueModel selectionValueModel, PropertyChangeListener... validationListener) {
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);
        super.bindJTable(table, columnModel, listModel, null, validationListener);
        table.setAutoCreateRowSorter(true);
        if (selectionValueModel != null) {
            SelectionInList<T> selectionInList = new SelectionInList<T>(listModel, selectionValueModel) {
                private static final long serialVersionUID = 0L;

                @Override
                protected T getSafeElementAt(int index) {
                    return (index < 0 || index >= getSize()) ? null : super.getSafeElementAt(table.getRowSorter().convertRowIndexToModel(index));
                }
            };
            for (PropertyChangeListener listener : validationListeners)
                selectionInList.addValueChangeListener(listener);
                    for (PropertyChangeListener listener : validationListener)
                        selectionInList.addValueChangeListener(listener);
                            table.setSelectionModel(new SingleListSelectionAdapter(selectionInList.getSelectionIndexHolder()));
        }
    }

    /**
     * Binds a JXTreeTable to a given ValueModel.
     * @param <T> - type
     * @param table - table to bind to
     * @param columnModel - column model of the table
     * @param model - list (row) model of the tree table
     * @param selectionValueModel - model of the selection value
     * @param validationListener - [optional] list of validation listeners
     */
    public <T>void bindJXTreeTable(final JXTreeTable table, TableColumnModel columnModel, DefaultTreeTableModel model, final ValueModel selectionValueModel,
            PropertyChangeListener... validationListener) {
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);
        table.setTreeTableModel(model);
        table.setAutoCreateRowSorter(true);
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumnExt tCol = table.getColumnExt(i);
            TableColumn sCol = columnModel.getColumn(i);
            tCol.setCellEditor(sCol.getCellEditor());
            tCol.setCellRenderer(sCol.getCellRenderer());
            tCol.setHeaderRenderer(sCol.getHeaderRenderer());
            tCol.setHeaderValue(sCol.getHeaderValue());
            tCol.setIdentifier(sCol.getIdentifier());
            tCol.setMaxWidth(sCol.getMaxWidth());
            tCol.setMinWidth(sCol.getMinWidth());
            tCol.setModelIndex(sCol.getModelIndex());
            tCol.setPreferredWidth(sCol.getPreferredWidth());
            tCol.setResizable(sCol.getResizable());
            tCol.setWidth(sCol.getWidth());
            for (PropertyChangeListener listener : tCol.getPropertyChangeListeners())
                tCol.removePropertyChangeListener(listener);
                    for (PropertyChangeListener listener : sCol.getPropertyChangeListeners())
                        tCol.addPropertyChangeListener(listener);
                            if (sCol instanceof TableColumnExt) {
                                TableColumnExt eCol = (TableColumnExt) sCol;
                                tCol.setComparator(eCol.getComparator());
                                tCol.setPrototypeValue(eCol.getPrototypeValue());
                                tCol.setTitle(eCol.getTitle());
                                tCol.setToolTipText(eCol.getToolTipText());
                                tCol.setEditable(eCol.isEditable());
                                tCol.setSortable(eCol.isSortable());
                                tCol.setVisible(eCol.isVisible());
                                tCol.setHighlighters(eCol.getHighlighters());
                                new TableColumnExtWrapper(tCol).copyClientPropertiesFrom(eCol);
                                // new
                                // TableColumnExtWrapper(eCol).copyClientPropertiesFrom(tCol);
                            }
        }
        if (selectionValueModel != null) {
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    selectionValueModel.setValue(e.getNewLeadSelectionPath() == null ? null : e.getNewLeadSelectionPath().getLastPathComponent());
                }
            });
            for (PropertyChangeListener listener : validationListeners)
                selectionValueModel.addValueChangeListener(listener);
                    for (PropertyChangeListener listener : validationListener)
                        selectionValueModel.addValueChangeListener(listener);
        }
    }
}

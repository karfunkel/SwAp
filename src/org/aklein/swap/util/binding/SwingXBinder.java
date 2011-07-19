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

public class SwingXBinder extends SwingBinder {
	private static SwingXBinder	sharedInstance	= new SwingXBinder();

	public static SwingXBinder getSharedInstance() {
		return sharedInstance;
	}

	public <T> void bindJXTable(final JXTable table, TableColumnModel columnModel, ListModel listModel, ValueModel selectionValueModel, PropertyChangeListener... validationListener) {
		super.bindJTable(table, columnModel, listModel, null, validationListener);
		if (selectionValueModel != null) {
			SelectionInList<T> selectionInList = new SelectionInList<T>(listModel, selectionValueModel) {
				private static final long	serialVersionUID	= 0L;

				@Override
				protected T getSafeElementAt(int index) {
					return (index < 0 || index >= getSize()) ? null : super.getSafeElementAt((table).getFilters().convertRowIndexToModel(index));
				}
			};
			for (PropertyChangeListener listener : validationListeners)
				selectionInList.addValueChangeListener(listener);
			for (PropertyChangeListener listener : validationListener)
				selectionInList.addValueChangeListener(listener);
			table.setSelectionModel(new SingleListSelectionAdapter(selectionInList.getSelectionIndexHolder()));
		}
	}

	public <T> void bindJXTreeTable(final JXTreeTable table, TableColumnModel columnModel, DefaultTreeTableModel model, final ValueModel selectionValueModel,
			PropertyChangeListener... validationListener) {
		table.setTreeTableModel(model);
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
				new TableColumnExtWrapper(eCol).copyClientPropertiesTo(tCol);
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

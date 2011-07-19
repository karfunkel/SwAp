package org.aklein.swap.util.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.beanutils.BeanUtils;
import org.jdesktop.swingx.table.TableColumnExt;

public class ListBasedTableModel<T> extends AbstractTableModel {
	private List<T>				list;
	private TableColumnModel	columnModel;

	public ListBasedTableModel(TableColumnModel columnModel) {
		this(columnModel, new ArrayList<T>());
	}

	public ListBasedTableModel(TableColumnModel columnModel, List<T> list) {
		super();
		this.list = list;
		this.columnModel = columnModel;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		TableColumn col = columnModel.getColumn(columnIndex);
		Object value = null;
		if (col instanceof TableColumnExt)
			value = ((TableColumnExt) col).getPrototypeValue();
		else
			value = getValueAt(0, columnIndex);
		if (value == null)
			return Object.class;
		else
			return value.getClass();
	}

	@Override
	public String getColumnName(int column) {
		TableColumn col = columnModel.getColumn(column);
		return col.getIdentifier().toString();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		TableColumn col = columnModel.getColumn(columnIndex);
		if (col instanceof TableColumnExt)
			return ((TableColumnExt) col).isEditable();
		else
			return col.getCellEditor() != null;
	}

	public int getColumnCount() {
		return columnModel.getColumnCount();
	}

	public int getRowCount() {
		return list.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return null;
		T row = list.get(rowIndex);
		if (row == null)
			return null;
		if (row.getClass().isArray()) {
			if (((Object[]) row).length <= columnIndex)
				return null;
			return ((Object[]) row)[columnIndex];
		}
		else if (row instanceof List) {
			if (((List) row).size() <= columnIndex)
				return null;
			return ((List) row).get(columnIndex);
		}
		else if (row instanceof Map) {
			String name = getColumnName(columnIndex);
			return ((Map) row).get(name);
		}
		else {
			String name = getColumnName(columnIndex);
			try {
				return BeanUtils.getProperty(row, name);
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (rowIndex >= list.size())
			return;
		T row = list.get(rowIndex);
		if (row == null)
			return;
		if (row.getClass().isArray()) {
			if (((Object[]) row).length <= columnIndex)
				return;
			((Object[]) row)[columnIndex] = value;
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
		else if (row instanceof List) {
			if (((List) row).size() <= columnIndex)
				return;
			((List) row).set(columnIndex, value);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
		else if (row instanceof Map) {
			String name = getColumnName(columnIndex);
			((Map) row).put(name, value);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
		else {
			String name = getColumnName(columnIndex);
			try {
				BeanUtils.setProperty(row, name, value);
				fireTableRowsUpdated(rowIndex, rowIndex);
			}
			catch (Exception e) {}
		}
	}

	public void addRow(T data) {
		list.add(data);
		fireTableRowsInserted(list.size() - 1, list.size() - 1);
	}

	public void addRow(int index, T data) {
		list.add(index, data);
		fireTableRowsInserted(index, index);
	}

	public void removeRow(T data) {
		int index = list.indexOf(data);
		list.remove(data);
		fireTableRowsDeleted(index, index);
	}

	public void removeRow(int index) {
		if (index < list.size()) {
			list.remove(index);
			fireTableRowsDeleted(index, index);
		}
	}

	public T getRow(int rowIndex) {
		if (rowIndex >= list.size())
			return null;
		return list.get(rowIndex);
	}

}

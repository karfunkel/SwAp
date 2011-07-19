package org.aklein.swap.examples.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.beanutils.PropertyUtils;
import org.jdesktop.swingx.table.TableColumnExt;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

public class DataTableModel extends AbstractTableAdapter<Data> {
	private static final long	serialVersionUID	= -3066042756568297298L;
	List<Data>					list;
	List<TableColumn>			columns;
	TableColumnModel			columnModel;

	public DataTableModel(TableColumnModel columnModel) {
		this(new ArrayList<Data>(), columnModel);
	}

	public DataTableModel(List<Data> list, TableColumnModel columnModel) {
		super(new GenericListModel<Data>(list));
		columns = getColumns(columnModel);
		this.list = list;
		this.columnModel = columnModel;
	}

	private List<TableColumn> getColumns(TableColumnModel columnModel) {
		Enumeration<TableColumn> e = columnModel.getColumns();
		List<TableColumn> cols = new ArrayList<TableColumn>();
		while (e.hasMoreElements())
			cols.add(e.nextElement());

		Collections.sort(cols, new Comparator<TableColumn>() {
			public int compare(TableColumn o1, TableColumn o2) {
				return o1.getModelIndex() - o2.getModelIndex();
			}
		});
		return cols;
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		TableColumn tc = columns.get(columnIndex);
		if (tc instanceof TableColumnExt)
			return ((TableColumnExt) tc).getTitle();
		else
			return (String) tc.getHeaderValue();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Data item = getRow(rowIndex);
		TableColumn tc = columns.get(columnIndex);
		try {
			return PropertyUtils.getProperty(item, tc.getIdentifier().toString());
		}
		catch (Exception e) {
			return null;
		}
	}

	public void add(Data item) {
		int pos = this.list.size();
		this.list.add(item);
		fireTableRowsInserted(pos, pos);
	}

	public static class GenericListModel<D> extends AbstractListModel {
		private static final long	serialVersionUID	= 447127336638805078L;
		List<D>						list;

		public GenericListModel(List<D> list) {
			super();
			this.list = list;
		}

		public Object getElementAt(int index) {
			return list.get(index);
		}

		public int getSize() {
			return list.size();
		}
	}
}

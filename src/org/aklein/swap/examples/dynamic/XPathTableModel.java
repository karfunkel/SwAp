package org.aklein.swap.examples.dynamic;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

@SuppressWarnings("serial")
public class XPathTableModel extends AbstractTableAdapter<Element> implements ListModel, TableColumnModelExt {

	private GenericModel				listModel;
	private DefaultTableColumnModelExt	columnModel;
	private String						root;
	private Document					doc;
	private URL							document;
	private Set<TableColumn>			columns;
	private String						itemName;
	private List<String>				exclusives;

	public XPathTableModel(URL document, String root, String itemName, String... exclusives) throws DocumentException {
		this(new GenericModel(), document, root, itemName, exclusives);
	}

	private XPathTableModel(GenericModel listModel, URL document, String root, String itemName, String... exclusives) throws DocumentException {
		super(listModel);
		listModel.setModel(this);
		this.listModel = listModel;
		this.root = root;
		this.columnModel = new DefaultTableColumnModelExt();
		this.columns = new TreeSet<TableColumn>();
		this.document = document;
		this.itemName = itemName;
		this.exclusives = Arrays.asList(exclusives);
		load(document);
	}

	public void reload() {
		try {
			load(document);
		}
		catch (DocumentException e) {}
	}

	public void load(URL document) throws DocumentException {
		SAXReader reader = new SAXReader();
		doc = reader.read(document);
		findColumns();
	}

	@SuppressWarnings("unchecked")
	private void findColumns() {
		List<TableColumn> cols = getColumns(true);
		for (TableColumn col : cols)
			removeColumn(col);
		columns.clear();

		for (Element elem : (List<Element>) doc.selectNodes(root + "/*")) {
			if (elem.getName().equals(itemName)) {
				for (Element e : (List<Element>) elem.elements()) {
					if (exclusives.contains(e.getName())) {}
					else if (!e.getName().equals(itemName)) {
						UniqueColumn column = new UniqueColumn(e.getName());
						column.setTitle(e.getName());
						columns.add(column);
					}
				}
			}
		}
		int i = 0;
		for (TableColumn column : columns) {
			column.setModelIndex(i);
			addColumn(column);
			i++;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		TableColumn column = columnModel.getColumn(columnIndex);
		String name = (String) column.getIdentifier();
		Element e = (Element) doc.selectSingleNode(root + "/" + itemName + "[" + (rowIndex + 1) + "]/" + name);
		return e.getTextTrim();
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
		findColumns();
		fireTableStructureChanged();
	}

	public static class GenericModel extends AbstractListModel {
		XPathTableModel	model;

		public GenericModel() {
			super();
		}

		public XPathTableModel getModel() {
			return model;
		}

		public void setModel(XPathTableModel model) {
			this.model = model;
		}

		public Object getElementAt(int index) {
			return getModel().doc.selectSingleNode(getModel().root + "/" + getModel().itemName + "[" + (index + 1) + "]");
		}

		public int getSize() {
			return getModel().doc.selectNodes(getModel().root + "/" + getModel().itemName).size();
		}
	}

	// ListModel delegates

	public void addListDataListener(ListDataListener l) {
		listModel.addListDataListener(l);
	}

	public Object getElementAt(int index) {
		return listModel.getElementAt(index);
	}

	public ListDataListener[] getListDataListeners() {
		return listModel.getListDataListeners();
	}

	@Override
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listModel.getListeners(listenerType);
	}

	public int getSize() {
		return listModel.getSize();
	}

	public void removeListDataListener(ListDataListener l) {
		listModel.removeListDataListener(l);
	}

	// TableColumnModel delegates

	public List<TableColumn> getColumns(boolean includeHidden) {
		return columnModel.getColumns(includeHidden);
	}

	public int getColumnCount(boolean includeHidden) {
		return columnModel.getColumnCount(includeHidden);
	}

	public TableColumnExt getColumnExt(int columnIndex) {
		return columnModel.getColumnExt(columnIndex);
	}

	public TableColumnExt getColumnExt(Object identifier) {
		return columnModel.getColumnExt(identifier);
	}

	public void addColumn(TableColumn column) {
		columnModel.addColumn(column);
	}

	public void moveColumn(int columnIndex, int newIndex) {
		columnModel.moveColumn(columnIndex, newIndex);
	}

	public void removeColumn(TableColumn column) {
		columnModel.removeColumn(column);
	}

	public void setColumnMargin(int newMargin) {
		columnModel.setColumnMargin(newMargin);
	}

	@Override
	public int getColumnCount() {
		return columnModel.getColumnCount();
	}

	public int getColumnIndex(Object identifier) {
		return columnModel.getColumnIndex(identifier);
	}

	public Enumeration<TableColumn> getColumns() {
		return columnModel.getColumns();
	}

	public void addColumnModelListener(TableColumnModelListener x) {
		columnModel.addColumnModelListener(x);
	}

	public TableColumn getColumn(int columnIndex) {
		return columnModel.getColumn(columnIndex);
	}

	public int getColumnIndexAtX(int position) {
		return columnModel.getColumnIndexAtX(position);
	}

	public int getColumnMargin() {
		return columnModel.getColumnMargin();
	}

	public boolean getColumnSelectionAllowed() {
		return columnModel.getColumnSelectionAllowed();
	}

	public int getSelectedColumnCount() {
		return columnModel.getSelectedColumnCount();
	}

	public int[] getSelectedColumns() {
		return columnModel.getSelectedColumns();
	}

	public ListSelectionModel getSelectionModel() {
		return columnModel.getSelectionModel();
	}

	public int getTotalColumnWidth() {
		return columnModel.getTotalColumnWidth();
	}

	public void removeColumnModelListener(TableColumnModelListener x) {
		columnModel.removeColumnModelListener(x);
	}

	public void setColumnSelectionAllowed(boolean flag) {
		columnModel.setColumnSelectionAllowed(flag);
	}

	public void setSelectionModel(ListSelectionModel newModel) {
		columnModel.setSelectionModel(newModel);
	}

	public static class UniqueColumn extends TableColumnExt implements Comparable<TableColumnExt> {
		public UniqueColumn(String identifier) {
			super();
			setIdentifier(identifier);
			setSortable(true);
			setEditable(false);
		}

		public int compareTo(TableColumnExt o) {
			return ((String) getIdentifier()).compareTo((String) o.getIdentifier());
		}
	}

}

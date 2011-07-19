package org.aklein.swap.util.binding;

import groovy.lang.Closure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.list.SelectionInList;

/**
 * A generic {@link TableModel} wrapping around a {@link ListModel}, bindable via {@link SelectionInList}.
 * 
 * @author Alexander Klein
 * @param <T>
 */
public class GenericTableAdapter<T> extends AbstractTableAdapter<T> {
	private static final long	serialVersionUID		= -5605785178685812392L;

	public static final String	PROPERTY_GETVALUE		= "getValue";
	public static final String	PROPERTY_RETURN_BEAN	= "returnBean";

	@SuppressWarnings("unused")
	private static Log			log						= LogFactory.getLog(GenericTableAdapter.class);
	private ListModel			listModel;
	private TableColumnModel	columnModel;
	private Class				tableColumnModelExt		= null;
	private Map<String, Method>	methods					= new HashMap<String, Method>();

	public GenericTableAdapter(ListModel listModel, TableColumnModel columnModel) {
		super(listModel);
		this.listModel = listModel;
		this.columnModel = columnModel;
	}

	@Override
	public int getColumnCount() {
		return columnModel.getColumnCount();
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (isTableColumnModelExt(columnModel))
			return (String) getProperty(columnModel, columnIndex, "getTitle", null, null);
		else
			return columnModel.getColumn(columnIndex).getHeaderValue().toString();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (isTableColumnModelExt(columnModel)) {
			Object prototype = getProperty(columnModel, columnIndex, "getPrototypeValue", null, null);
			if (prototype == null)
				return Object.class;
			else
				return prototype.getClass();
		}
		else
			return Object.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (isTableColumnModelExt(columnModel))
			return (Boolean) getProperty(columnModel, columnIndex, "isEditable", null, null);
		else
			return true;
	}

	/**
	 * @return the listModel
	 */
	public ListModel getListModel() {
		return listModel;
	}

	/**
	 * @return the columnModel
	 */
	public TableColumnModel getColumnModel() {
		return columnModel;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		Object bean = listModel.getElementAt(rowIndex);
		String id = getColumnIndex(columnIndex);
		try {
			setValue(bean, id, value);
		}
		catch (Exception e) {
			log.error("Reading value in row " + rowIndex + " column " + columnIndex + ": Error reading property " + id + " from bean " + bean, e);
		}
	}

	protected void setValue(Object bean, String property, Object value) throws IllegalAccessException, InvocationTargetException {
		BeanUtils.setProperty(bean, property, value);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object bean = listModel.getElementAt(rowIndex);
		String id = getColumnIndex(columnIndex);

		if (isTableColumnModelExt(columnModel)) {
			Closure getValue = (Closure) getProperty(columnModel, columnIndex, "getClientProperty", new Class[] { Object.class }, new Object[] { PROPERTY_GETVALUE });
			if (getValue != null)
				return getValue.call(bean);

			Boolean ro = (Boolean) getProperty(columnModel, columnIndex, "getClientProperty", new Class[] { Object.class }, new Object[] { PROPERTY_RETURN_BEAN });
			if (ro != null && ro)
				return bean;
		}

		if (bean.getClass().isArray()) {
			return ((Object[]) bean)[columnIndex];
		}

		if (bean instanceof List) {
			return ((List<?>) bean).get(columnIndex);
		}

		try {
			return getValue(bean, id);
		}
		catch (Exception e) {
			log.error("Reading value in row " + rowIndex + " column " + columnIndex + ": Error reading property " + id + " from bean " + bean, e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private String getColumnIndex(int columnIndex) {
		String id = "";
		if (isTableColumnModelExt(columnModel)) {
			Method getColumns = methods.get("getColumns");
			if (getColumns == null) {
				try {
					getColumns = columnModel.getClass().getMethod("getColumns", boolean.class);
					methods.put("getColumns", getColumns);
				}
				catch (SecurityException e) {
					return id;
				}
				catch (NoSuchMethodException e) {
					return id;
				}
				catch (IllegalArgumentException e) {
					return id;
				}
			}
			try {
				List<TableColumn> cols = (List<TableColumn>) getColumns.invoke(columnModel, true);
				id = cols.get(columnIndex).getIdentifier().toString();
			}
			catch (IllegalAccessException e) {
				return id;
			}
			catch (InvocationTargetException e) {
				return id;
			}
		}
		else
			id = columnModel.getColumn(columnIndex).getIdentifier().toString();
		return id;
	}

	protected Object getValue(Object bean, String property) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		try {
			return PropertyUtils.getProperty(bean, property);
		}
		catch (Exception e) {
			try {
				return MethodUtils.invokeMethod(bean, "get", property);
			}
			catch (Exception e1) {
				try {
					return MethodUtils.invokeMethod(bean, "getProperty", property);
				}
				catch (Exception e2) {
					return null;
				}
			}
		}
	}

	private Object getProperty(TableColumnModel columnModel, int columnIndex, String getter, Class[] getterTypes, Object[] getterParams) {
		Method getColumnExt = methods.get("getColumnExt");
		if (getColumnExt == null) {
			try {
				getColumnExt = columnModel.getClass().getMethod("getColumnExt", int.class);
				methods.put("getColumnExt", getColumnExt);
			}
			catch (SecurityException e) {
				log.error("", e);
				return null;
			}
			catch (NoSuchMethodException e) {
				log.error("", e);
				return null;
			}
			catch (IllegalArgumentException e) {
				log.error("", e);
				return null;
			}
		}
		Object tce;
		try {
			tce = getColumnExt.invoke(columnModel, columnIndex);
		}
		catch (IllegalArgumentException e) {
			log.error("", e);
			return null;
		}
		catch (IllegalAccessException e) {
			log.error("", e);
			return null;
		}
		catch (InvocationTargetException e) {
			log.error("", e);
			return null;
		}
		Method getterMethod = methods.get(getter);
		if (getterMethod == null) {
			try {
				getterMethod = tce.getClass().getMethod(getter, getterTypes);
				methods.put(getter, getterMethod);
			}
			catch (SecurityException e) {
				log.error("", e);
				return null;
			}
			catch (NoSuchMethodException e) {
				log.error("", e);
				return null;
			}
			catch (IllegalArgumentException e) {
				log.error("", e);
				return null;
			}
		}
		try {
			return getterMethod.invoke(tce, getterParams);
		}
		catch (IllegalArgumentException e) {
			log.error("", e);
			return null;
		}
		catch (IllegalAccessException e) {
			log.error("", e);
			return null;
		}
		catch (InvocationTargetException e) {
			log.error("", e);
			return null;
		}
	}

	private boolean isTableColumnModelExt(TableColumnModel model) {
		if (tableColumnModelExt == null) {
			try {
				tableColumnModelExt = Class.forName("org.jdesktop.swingx.table.TableColumnModelExt");
			}
			catch (ClassNotFoundException e) {
				tableColumnModelExt = e.getClass();
				return false;
			}
		}
		return tableColumnModelExt.isAssignableFrom(model.getClass());
	}
}

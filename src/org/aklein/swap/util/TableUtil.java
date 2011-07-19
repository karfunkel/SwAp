package org.aklein.swap.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.CustomConverter;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Utility to fill a {@link TableColumnModel} with {@link TableColumnExt}s specified in {@link Configuration}.<br/>
 * <br/>
 * see conf/tables.xml for more infos
 * 
 * @author Alexander Klein
 */
public class TableUtil {
    private static Log log = LogFactory.getLog(TableUtil.class);

    /**
     * reload the configuration
     * 
     * @param config
     * @param model
     * @param key
     */
    public static void reloadTableColumnModel(Configuration config, TableColumnModel model, String key) {
	Enumeration<TableColumn> e = model.getColumns();
	List<TableColumn> list = new ArrayList<TableColumn>();
	while (e.hasMoreElements())
	    list.add(e.nextElement());
	for (TableColumn tc : list)
	    model.removeColumn(tc);
	fillTableColumns(config, model, key);
    }

    /**
     * create a new TableColumnModel
     * 
     * @param config
     *            {@link Configuration} to load data from
     * @param key
     *            subkey of the table in config (-> tables.[key])
     * @return
     */
    public static TableColumnModel createTableColumnModel(Configuration config, String key) {
	TableColumnModel model = new ExtendableTableColumnModelExt();
	fillTableColumns(config, model, key);
	return model;
    }

    /**
     * fill the configuration
     * 
     * @param config
     *            {@link Configuration} to load data from
     * @param model
     *            {@link TableModel} to fill
     * @param key
     *            subkey of the table in config (-> tables.[key]) TODO: create a version that does not need a bean to
     *            get the class of the property
     */
    @SuppressWarnings("unchecked")
    public static void fillTableColumns(Configuration config, TableColumnModel model, String key) {
	if (model instanceof ExtendableTableColumnModelExt) {
	    HierarchicalConfiguration sub = ((HierarchicalConfiguration) config).configurationAt("tables." + key);
	    for (Iterator iterator = sub.getKeys(); iterator.hasNext();) {
		String name = (String) iterator.next();
		String value = sub.getString(name);
		if ((value != null) && (value.length() > 0)) {
		    if (name.startsWith("@"))
			((ExtendableTableColumnModelExt) model).putClientProperty(name.substring(1), value);
		    else if ((!name.startsWith("column")) && (!name.contains("@")) && (!name.contains(".")))
			((ExtendableTableColumnModelExt) model).putClientProperty(name, getValue(key, 0, null, sub, name, config, model, name, value));
		}
	    }

	}
	// get list of all columns in table
	Object prop = config.getProperty("tables." + key + ".column[@property]");
	List<String> properties;
	// only one column
	if (prop instanceof String) {
	    properties = new ArrayList<String>();
	    properties.add((String) prop);
	}
	// multiple columns
	else if (prop instanceof List)
	    properties = (List<String>) prop;
	else {
	    log.error("Error reading columns in tables." + key + ". " + prop + " is not a valid result of columns");
	    return;
	}
	// traverse columns
	for (int i = 0; i < properties.size(); i++) {
	    TableColumnExt column = new TableColumnExt();
	    String property = properties.get(i);
	    column.setIdentifier(property);

	    // create subconfig for column
	    HierarchicalConfiguration sub = ((HierarchicalConfiguration) config).configurationAt("tables." + key + ".column(" + i + ")");
	    for (Iterator iterator = sub.getKeys(); iterator.hasNext();) {
		String name = (String) iterator.next();
		// ignore attributes of column
		if (name.indexOf('@') > 0)
		    continue;
		boolean customProperty = false;
		String value = sub.getString(name);
		if ((value != null) && (value.length() > 0)) {
		    customProperty = isCustomProperty(column, name);
		    Object val = getValue(key, i, column, sub, name, config, model, property, value);
		    try {
			// set property of TableColumnExt
			if (val != null) {
			    if (customProperty)
				column.putClientProperty(name, val);
			    else
				PropertyUtils.setSimpleProperty(column, name, val);
			}
		    } catch (Exception e) {
			log.warn("Could not set property " + name + " for column " + property, e);
		    }
		}
	    }
	    // add column to model
	    model.addColumn(column);
	}
    }

    private static Object getValue(String key, int i, TableColumnExt column, HierarchicalConfiguration sub, String name, Configuration config, TableColumnModel model, String property, String value) {
	boolean script = sub.getBoolean(name + "[@script]", false);
	Class type = getType(key, i, column, sub, name);
	Object val = null;
	String scriptLang = config.getString("tables[@language]", "groovy");
	if (script) {
	    ScriptingWrapper bsm = new ScriptingWrapper(scriptLang);
	    try {
		// set variables for script
		bsm.put("config", config);
		bsm.put("subConfig", sub);
		bsm.put("model", model);
		bsm.put("key", key);
		bsm.put("cls", type);
		bsm.put("property", property);
		if (column != null)
		    bsm.put("column", column);
		// run script
		Object r = bsm.eval(value, key);
		// only use result if it is != null and from required Class
		if ((r != null) && (type.isAssignableFrom(r.getClass())))
		    val = r;
	    } catch (Exception ex) {
		log.warn("Error running Tablescript: tables." + key + ".column(" + i + ")." + name, ex);
		return null;
	    }
	} else {
	    try {
		// convert config string to correct type
		if ((String.class.isAssignableFrom(type)) || (Object.class.equals(type)))
		    val = value;
		else if (Date.class.isAssignableFrom(type))
		    val = CustomConverter.to(type, value, "yyyy-MM-dd HH:mm:ss.SS");
		else if (Calendar.class.isAssignableFrom(type))
		    val = CustomConverter.to(type, value, "yyyy-MM-dd HH:mm:ss.SS");
		else
		    val = CustomConverter.to(type, value);
	    } catch (org.apache.commons.configuration.ConversionException e) {
		log.warn("Error " + name + " in tables." + key + ".column(" + i + ")." + name + " could not be converted to " + type.getName(), e);
		return null;
	    }
	}
	return val;
    }

    private static Class getType(String key, int i, TableColumnExt column, HierarchicalConfiguration sub, String name) {
	Class c = null;
	try {
	    // get property from TableColumnExt by name (e.g. <title> -> setTitle())
	    c = PropertyUtils.getPropertyType(column, name);
	} catch (Exception e) {
	}
	if (c == null) {
	    c = Object.class;
	}
	String type = sub.getString(name + "[@type]");
	if (type != null) {
	    try {
		if ("boolean".equals(type))
		    c = boolean.class;
		else if ("int".equals(type))
		    c = int.class;
		else if ("double".equals(type))
		    c = double.class;
		else if ("float".equals(type))
		    c = float.class;
		else if ("short".equals(type))
		    c = short.class;
		else if ("long".equals(type))
		    c = long.class;
		else if ("byte".equals(type))
		    c = byte.class;
		else if ("char".equals(type))
		    c = char.class;
		else if ("String".equals(type))
		    c = String.class;
		else
		    c = Class.forName(type);
	    } catch (ClassNotFoundException e) {
		log.error("Could not find type '" + type + "' at: tables." + key + ".column(" + i + ")." + name, e);
	    }
	}
	return c;
    }

    private static boolean isCustomProperty(TableColumn column, String name) {
	Class c = null;
	try {
	    // get property from TableColumnExt by name (e.g. <title> -> setTitle())
	    c = PropertyUtils.getPropertyType(column, name);
	} catch (Exception e) {
	}
	return (c == null);
    }
}

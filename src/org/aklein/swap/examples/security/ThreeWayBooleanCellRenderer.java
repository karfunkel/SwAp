package org.aklein.swap.examples.security;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;

import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

public class ThreeWayBooleanCellRenderer extends DefaultTableRenderer
{
	public ThreeWayBooleanCellRenderer()
	{
		super(new CheckBoxProvider());
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (value == null)
		{
			JCheckBox c = (JCheckBox) super.getTableCellRendererComponent(table, false, isSelected, hasFocus, row, column);
			c.setEnabled(false);
			return c;
		}
		else
		{
			JCheckBox c = (JCheckBox) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setEnabled(true);
			return c;
		}
	}

}

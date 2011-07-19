package org.jdesktop.swingx.table;

import java.beans.PropertyChangeListener;
import java.util.Comparator;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.decorator.Highlighter;

public class TableColumnExtWrapper extends TableColumnExt {
    private final TableColumnExt	delegate;

    public TableColumnExtWrapper(TableColumnExt delegate) {
        this.delegate = delegate;
    }

    /**
     * @param highlighter
     * @see org.jdesktop.swingx.table.TableColumnExt#addHighlighter(org.jdesktop.swingx.decorator.Highlighter)
     */
    @Override
    public void addHighlighter(Highlighter highlighter) {
        delegate.addHighlighter(highlighter);
    }

    /**
     * @param listener
     * @see javax.swing.table.TableColumn#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    /**
     * @param copy
     * @see org.jdesktop.swingx.table.TableColumnExt#copyClientPropertiesTo(org.jdesktop.swingx.table.TableColumnExt)
     */
    @Override
    public void copyClientPropertiesFrom(TableColumnExt original) {
        delegate.copyClientPropertiesFrom(original);
    }

    /**
     * @deprecated
     * @see javax.swing.table.TableColumn#disableResizedPosting()
     */
    @Deprecated
    @Override
    public void disableResizedPosting() {
        delegate.disableResizedPosting();
    }

    /**
     * @deprecated
     * @see javax.swing.table.TableColumn#enableResizedPosting()
     */
    @Deprecated
    @Override
    public void enableResizedPosting() {
        delegate.enableResizedPosting();
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getCellEditor()
     */
    @Override
    public TableCellEditor getCellEditor() {
        return delegate.getCellEditor();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getCellRenderer()
     */
    @Override
    public TableCellRenderer getCellRenderer() {
        return delegate.getCellRenderer();
    }

    /**
     * @param key
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getClientProperty(java.lang.Object)
     */
    @Override
    public Object getClientProperty(Object key) {
        return delegate.getClientProperty(key);
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getComparator()
     */
    @Override
    public Comparator getComparator() {
        return delegate.getComparator();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getHeaderRenderer()
     */
    @Override
    public TableCellRenderer getHeaderRenderer() {
        return delegate.getHeaderRenderer();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getHeaderValue()
     */
    @Override
    public Object getHeaderValue() {
        return delegate.getHeaderValue();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getHighlighters()
     */
    @Override
    public Highlighter[] getHighlighters() {
        return delegate.getHighlighters();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getIdentifier()
     */
    @Override
    public Object getIdentifier() {
        return delegate.getIdentifier();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getMaxWidth()
     */
    @Override
    public int getMaxWidth() {
        return delegate.getMaxWidth();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getMinWidth()
     */
    @Override
    public int getMinWidth() {
        return delegate.getMinWidth();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getModelIndex()
     */
    @Override
    public int getModelIndex() {
        return delegate.getModelIndex();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getPreferredWidth()
     */
    @Override
    public int getPreferredWidth() {
        return delegate.getPreferredWidth();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getPropertyChangeListeners()
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return delegate.getPropertyChangeListeners();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getPrototypeValue()
     */
    @Override
    public Object getPrototypeValue() {
        return delegate.getPrototypeValue();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getResizable()
     */
    @Override
    public boolean getResizable() {
        return delegate.getResizable();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getTitle()
     */
    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        return delegate.getToolTipText();
    }

    /**
     * @return
     * @see javax.swing.table.TableColumn#getWidth()
     */
    @Override
    public int getWidth() {
        return delegate.getWidth();
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#isEditable()
     */
    @Override
    public boolean isEditable() {
        return delegate.isEditable();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#isSortable()
     */
    @Override
    public boolean isSortable() {
        return delegate.isSortable();
    }

    /**
     * @return
     * @see org.jdesktop.swingx.table.TableColumnExt#isVisible()
     */
    @Override
    public boolean isVisible() {
        return delegate.isVisible();
    }

    /**
     * @param key
     * @param value
     * @see org.jdesktop.swingx.table.TableColumnExt#putClientProperty(java.lang.Object, java.lang.Object)
     */
    @Override
    public void putClientProperty(Object key, Object value) {
        delegate.putClientProperty(key, value);
    }

    /**
     * @param highlighter
     * @see org.jdesktop.swingx.table.TableColumnExt#removeHighlighter(org.jdesktop.swingx.decorator.Highlighter)
     */
    @Override
    public void removeHighlighter(Highlighter highlighter) {
        delegate.removeHighlighter(highlighter);
    }

    /**
     * @param listener
     * @see javax.swing.table.TableColumn#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    /**
     * @param cellEditor
     * @see javax.swing.table.TableColumn#setCellEditor(javax.swing.table.TableCellEditor)
     */
    @Override
    public void setCellEditor(TableCellEditor cellEditor) {
        delegate.setCellEditor(cellEditor);
    }

    /**
     * @param cellRenderer
     * @see javax.swing.table.TableColumn#setCellRenderer(javax.swing.table.TableCellRenderer)
     */
    @Override
    public void setCellRenderer(TableCellRenderer cellRenderer) {
        delegate.setCellRenderer(cellRenderer);
    }

    /**
     * @param comparator
     * @see org.jdesktop.swingx.table.TableColumnExt#setComparator(java.util.Comparator)
     */
    @Override
    public void setComparator(Comparator comparator) {
        delegate.setComparator(comparator);
    }

    /**
     * @param editable
     * @see org.jdesktop.swingx.table.TableColumnExt#setEditable(boolean)
     */
    @Override
    public void setEditable(boolean editable) {
        delegate.setEditable(editable);
    }

    /**
     * @param headerRenderer
     * @see javax.swing.table.TableColumn#setHeaderRenderer(javax.swing.table.TableCellRenderer)
     */
    @Override
    public void setHeaderRenderer(TableCellRenderer headerRenderer) {
        delegate.setHeaderRenderer(headerRenderer);
    }

    /**
     * @param headerValue
     * @see javax.swing.table.TableColumn#setHeaderValue(java.lang.Object)
     */
    @Override
    public void setHeaderValue(Object headerValue) {
        delegate.setHeaderValue(headerValue);
    }

    /**
     * @param highlighters
     * @see org.jdesktop.swingx.table.TableColumnExt#setHighlighters(org.jdesktop.swingx.decorator.Highlighter[])
     */
    @Override
    public void setHighlighters(Highlighter... highlighters) {
        delegate.setHighlighters(highlighters);
    }

    /**
     * @param identifier
     * @see javax.swing.table.TableColumn#setIdentifier(java.lang.Object)
     */
    @Override
    public void setIdentifier(Object identifier) {
        delegate.setIdentifier(identifier);
    }

    /**
     * @param maxWidth
     * @see javax.swing.table.TableColumn#setMaxWidth(int)
     */
    @Override
    public void setMaxWidth(int maxWidth) {
        delegate.setMaxWidth(maxWidth);
    }

    /**
     * @param minWidth
     * @see javax.swing.table.TableColumn#setMinWidth(int)
     */
    @Override
    public void setMinWidth(int minWidth) {
        delegate.setMinWidth(minWidth);
    }

    /**
     * @param modelIndex
     * @see javax.swing.table.TableColumn#setModelIndex(int)
     */
    @Override
    public void setModelIndex(int modelIndex) {
        delegate.setModelIndex(modelIndex);
    }

    /**
     * @param preferredWidth
     * @see javax.swing.table.TableColumn#setPreferredWidth(int)
     */
    @Override
    public void setPreferredWidth(int preferredWidth) {
        delegate.setPreferredWidth(preferredWidth);
    }

    /**
     * @param value
     * @see org.jdesktop.swingx.table.TableColumnExt#setPrototypeValue(java.lang.Object)
     */
    @Override
    public void setPrototypeValue(Object value) {
        delegate.setPrototypeValue(value);
    }

    /**
     * @param isResizable
     * @see javax.swing.table.TableColumn#setResizable(boolean)
     */
    @Override
    public void setResizable(boolean isResizable) {
        delegate.setResizable(isResizable);
    }

    /**
     * @param sortable
     * @see org.jdesktop.swingx.table.TableColumnExt#setSortable(boolean)
     */
    @Override
    public void setSortable(boolean sortable) {
        delegate.setSortable(sortable);
    }

    /**
     * @param title
     * @see org.jdesktop.swingx.table.TableColumnExt#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        delegate.setTitle(title);
    }

    /**
     * @param toolTipText
     * @see org.jdesktop.swingx.table.TableColumnExt#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String toolTipText) {
        delegate.setToolTipText(toolTipText);
    }

    /**
     * @param visible
     * @see org.jdesktop.swingx.table.TableColumnExt#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        delegate.setVisible(visible);
    }

    /**
     * @param width
     * @see javax.swing.table.TableColumn#setWidth(int)
     */
    @Override
    public void setWidth(int width) {
        delegate.setWidth(width);
    }

    /**
     * @see javax.swing.table.TableColumn#sizeWidthToFit()
     */
    @Override
    public void sizeWidthToFit() {
        delegate.sizeWidthToFit();
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

}

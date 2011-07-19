package org.aklein.swap.util.swing;

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

public class LimitedDocument implements Document {

	private final Document	delegate;
	private int				maxLength	= 0;
	private boolean			toUppercase	= false;

	public static void install(JTextField field) {
		install(field, 0, false);
	}

	public static void install(JTextField field, int maxLength) {
		install(field, maxLength, false);
	}

	public static void install(JTextField field, int maxLength, boolean toUppercase) {
		Document doc = field.getDocument();
		if (doc instanceof LimitedDocument) {
			((LimitedDocument) doc).setToUppercase(toUppercase);
			((LimitedDocument) doc).setMaxLength(maxLength);
		}
		else
			field.setDocument(new LimitedDocument(doc, maxLength, toUppercase));
	}

	public LimitedDocument(Document delegate) {
		this(delegate, 0, false);
	}

	public LimitedDocument(Document delegate, int maxLength) {
		this(delegate, maxLength, false);
	}

	public LimitedDocument(Document delegate, int maxLength, boolean toUppercase) {
		this.delegate = delegate;
		this.maxLength = maxLength;
		this.toUppercase = toUppercase;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public boolean isToUppercase() {
		return toUppercase;
	}

	public void setToUppercase(boolean toUppercase) {
		this.toUppercase = toUppercase;
	}

	public void addDocumentListener(DocumentListener listener) {
		delegate.addDocumentListener(listener);
	}

	public void addUndoableEditListener(UndoableEditListener listener) {
		delegate.addUndoableEditListener(listener);
	}

	public Position createPosition(int offs) throws BadLocationException {
		return delegate.createPosition(offs);
	}

	public Element getDefaultRootElement() {
		return delegate.getDefaultRootElement();
	}

	public Position getEndPosition() {
		return delegate.getEndPosition();
	}

	public int getLength() {
		return delegate.getLength();
	}

	public Object getProperty(Object key) {
		return delegate.getProperty(key);
	}

	public Element[] getRootElements() {
		return delegate.getRootElements();
	}

	public Position getStartPosition() {
		return delegate.getStartPosition();
	}

	public void getText(int offset, int length, Segment txt) throws BadLocationException {
		delegate.getText(offset, length, txt);
	}

	public String getText(int offset, int length) throws BadLocationException {
		return delegate.getText(offset, length);
	}

	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		if (str == null)
			return;
		if ((getMaxLength() > 0) && ((getLength() + str.length()) > getMaxLength()))
			str = str.substring(0, getMaxLength() - getLength());
		if (isToUppercase())
			str = str.toUpperCase();
		delegate.insertString(offset, str, a);
	}

	public void putProperty(Object key, Object value) {
		delegate.putProperty(key, value);
	}

	public void remove(int offs, int len) throws BadLocationException {
		delegate.remove(offs, len);
	}

	public void removeDocumentListener(DocumentListener listener) {
		delegate.removeDocumentListener(listener);
	}

	public void removeUndoableEditListener(UndoableEditListener listener) {
		delegate.removeUndoableEditListener(listener);
	}

	public void render(Runnable r) {
		delegate.render(r);
	}

}

package org.aklein.swap.examples.security;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.Key;

import javax.swing.JFormattedTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.aklein.swap.examples.security.DefaultUserManager.User;
import org.aklein.swap.ui.AbeilleViewControllerDialog;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import com.jeta.forms.gui.common.FormException;

public class UserDialog extends AbeilleViewControllerDialog {
	private static Log	log		= LogFactory.getLog(UserDialog.class);
	private User		user	= new User();
	private User		result;
	private boolean		ok		= false;
	private File		keyFolder;
	private Window		parent;

	public UserDialog() throws FormException {
		super(UserDialog.class.getResourceAsStream("resources/UserEditor.jfrm"), Application.getInstance(SingleFrameApplication.class).getMainFrame());
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setModal(true);
	}

	public void open(Window parent, File keyFolder, User user) {
		this.result = user;
		this.user.copy(user);
		this.user.getConfig().setProperty("name", user.getName());
		getTfName().setText(this.user.getConfig().getString("name"));
		getTfDescription().setText(this.user.getConfig().getString("description"));
		getBtnSave().setEnabled(user.getSecureKey() != null || user.getEncryptedMasterKey() != null);
		this.pack();
		this.setVisible(true);
	}

	@Override
	protected Component[] focusComponents() {
		return new Component[] { getTfName(), getTfDescription(), getBtnPassword(), getBtnSave(), getBtnCancel() };
	}

	@Override
	protected Dimension getDimension() {
		return new Dimension(500, 200);
	}

	@Override
	protected void initComponents() {
		bind(getTfName(), "name");
		bind(getTfDescription(), "description");

		getBtnSave().setAction(getAction("save"));
		getBtnCancel().setAction(getAction("cancel"));
		getBtnPassword().setAction(getAction("password"));
	}

	private void bind(final JFormattedTextField c, final String property) {
		c.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setValue(e);
			}

			public void insertUpdate(DocumentEvent e) {
				setValue(e);
			}

			public void removeUpdate(DocumentEvent e) {
				setValue(e);
			}

			private void setValue(DocumentEvent e) {
				try {
					user.getConfig().setProperty(property, e.getDocument().getText(0, e.getDocument().getLength()));
				}
				catch (BadLocationException e1) {
					log.error("Error getting document value", e1);
				}
			}
		});
	}

	public String getMessageKey() {
		return "UserDialog";
	}

	public User getUser() {
		return result;
	}

	public boolean isOk() {
		return ok;
	}

	@Action
	public void password(ActionEvent e) {
		try {
			Key key = PasswordDialog.open(getParent(), getResourceMap().getString("userPassword"), getKeyFolder(), true);
			if (key != null) {
				user.setSecureKey(key);
				user.updateEncryptedMasterKey();
				getBtnSave().setEnabled(true);
			}
		}
		catch (ConfigurationException e1) {
			log.error("Error setting password", e1);
		}
	}

	@Action
	public void save(ActionEvent e) {
		try {
			user.updateEncryptedMasterKey();
		}
		catch (ConfigurationException e1) {
			log.error("Error setting password", e1);
		}
		result = user;
		ok = true;
		this.setVisible(false);
	}

	@Action
	public void cancel(ActionEvent e) {
		ok = false;
		this.setVisible(false);
	}

	public File getKeyFolder() {
		return keyFolder;
	}

	public void setKeyFolder(File keyFolder) {
		Object old = this.keyFolder;
		this.keyFolder = keyFolder;
		firePropertyChange("keyFolder", old, keyFolder);
	}

	@Override
	public Window getParent() {
		return parent;
	}

	public void setParent(Window parent) {
		Object old = this.parent;
		this.parent = parent;
		firePropertyChange("parent", old, parent);
	}

	public User getResult() {
		return result;
	}

	// Generation 'UserEditor' START
	public com.jeta.forms.components.label.JETALabel getLblName() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblName");
	}

	public com.jeta.forms.components.label.JETALabel getLblDescription() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblDescription");
	}

	public javax.swing.JFormattedTextField getTfName() {
		return (javax.swing.JFormattedTextField) getView().getProperty("tfName");
	}

	public javax.swing.JFormattedTextField getTfDescription() {
		return (javax.swing.JFormattedTextField) getView().getProperty("tfDescription");
	}

	public javax.swing.JButton getBtnSave() {
		return (javax.swing.JButton) getView().getProperty("btnSave");
	}

	public javax.swing.JButton getBtnCancel() {
		return (javax.swing.JButton) getView().getProperty("btnCancel");
	}

	public javax.swing.JButton getBtnPassword() {
		return (javax.swing.JButton) getView().getProperty("btnPassword");
	}
	// Generation 'UserEditor' END
}

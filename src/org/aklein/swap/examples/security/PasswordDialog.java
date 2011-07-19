package org.aklein.swap.examples.security;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.swing.JFileChooser;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.aklein.swap.security.CryptTool;
import org.aklein.swap.security.XXmlConfiguration;
import org.aklein.swap.ui.AbeilleViewControllerDialog;
import org.aklein.swap.util.binding.Binder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Action;

import com.jeta.forms.gui.common.FormException;
import com.jgoodies.binding.beans.PropertyAdapter;

public class PasswordDialog extends AbeilleViewControllerDialog {
	private String		password;
	private String		confirmation;
	private File		keyFolder;
	private Key			key;
	private static Log	log	= LogFactory.getLog(PasswordDialog.class);

	protected PasswordDialog(Frame parent) throws FormException {
		super(PasswordDialog.class.getResourceAsStream("resources/PasswordDialog.jfrm"), parent);
		setTitle(getResourceMap().getString("title"));
	}

	protected PasswordDialog(Dialog parent) throws FormException {
		super(PasswordDialog.class.getResourceAsStream("resources/PasswordDialog.jfrm"), parent);
		setTitle(getResourceMap().getString("title"));
	}

	@Override
	protected Component[] focusComponents() {
		return new Component[] { getTfPassword(), getTfConfirmation(), getBtnKeyFile(), getBtnCancel(), getBtnOk() };
	}

	@Override
	protected Dimension getDimension() {
		return new Dimension(500, 200);
	}

	public static Key open(Window parent, String description, File keyFolder, boolean enableKeySelection) {
		try {
			PasswordDialog pd;
			if (parent instanceof Frame)
				pd = new PasswordDialog((Frame) parent);
			else if (parent instanceof Dialog)
				pd = new PasswordDialog((Frame) parent);
			else
				pd = new PasswordDialog((Frame) null);

			pd.init(null);
			pd.getLblDescription().setText(description);
			pd.setKeyFolder(keyFolder);
			pd.getBtnKeyFile().setVisible(enableKeySelection);
			pd.getBtnOk().setEnabled(false);
			pd.pack();
			pd.setVisible(true);

			return pd.getKey();
		}
		catch (FormException e) {
			log.error("Error loading form", e);
			return null;
		}
	}

	public static String getPassword(Window parent, String description) {
		try {
			PasswordDialog pd;
			if (parent instanceof Frame)
				pd = new PasswordDialog((Frame) parent);
			else if (parent instanceof Dialog)
				pd = new PasswordDialog((Frame) parent);
			else
				pd = new PasswordDialog((Frame) null);

			pd.init(null);
			pd.getLblDescription().setText(description);
			pd.getBtnKeyFile().setVisible(false);
			pd.getBtnOk().setEnabled(false);
			pd.pack();
			pd.setVisible(true);
			Key k = pd.getKey();
			if (k == null)
				return null;
			else
				return pd.getPassword();
		}
		catch (FormException e) {
			log.error("Error loading form", e);
			return null;
		}
	}

	@Override
	protected void initComponents() {
		PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				boolean ok = false;
				if (getPassword() != null)
					ok = getPassword().equals(getConfirmation());
				if (key != null)
					ok = true;
				getBtnOk().setEnabled(ok);
			}
		};
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setModal(true);
		Binder.bindPasswordField(getTfPassword(), new PropertyAdapter<PasswordDialog>(this, "password", true), true);
		Binder.bindPasswordField(getTfConfirmation(), new PropertyAdapter<PasswordDialog>(this, "confirmation", true), true);

		getBtnOk().setAction(getAction("ok"));
		getBtnCancel().setAction(getAction("cancel"));
		getBtnKeyFile().setAction(getAction("keyFile"));

		addPropertyChangeListener("password", listener);
		addPropertyChangeListener("confirmation", listener);
		addPropertyChangeListener("key", listener);
	}

	@Action
	public void ok(ActionEvent e) {
		if (key == null) {
			try {
				setKey(XXmlConfiguration.getCryptTool().createPBEKey(getPassword()));
			}
			catch (NoSuchAlgorithmException e1) {
				log.error("Error creating key from password", e1);
				setKey(null);
			}
			catch (NoSuchProviderException e1) {
				log.error("Error creating key from password", e1);
				setKey(null);
			}
		}
		setVisible(false);
	}

	@Action
	public void cancel(ActionEvent e) {
		setKey(null);
		setVisible(false);
	}

	@Action
	public void keyFile(ActionEvent e) {
		JFileChooser jfc = new JFileChooser(getKeyFolder());
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		jfc.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".priv") || f.getName().endsWith(".pub") || f.getName().endsWith(".key");
			}

			@Override
			public String getDescription() {
				return getResourceMap().getString("filter.keys");
			}
		});
		int result = jfc.showDialog(this, getResourceMap().getString("select"));
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			try {
				String keyFile = CryptTool.readComplete(new FileReader(file));
				if (XXmlConfiguration.getCryptTool().isEncrypted(keyFile)) {
					String pwd = PasswordDialog.getPassword(this, getResourceMap().getString("description.key.password"));
					if (pwd != null)
						setKey(XXmlConfiguration.getCryptTool().parseKey(keyFile, pwd));
				}
				else
					setKey(XXmlConfiguration.getCryptTool().parseKey(keyFile));
				setVisible(false);
			}
			catch (FileNotFoundException e1) {
				log.error("Error reading keyfile", e1);
				setKey(null);
			}
			catch (IOException e1) {
				log.error("Error reading keyfile", e1);
				setKey(null);
			}
			catch (GeneralSecurityException e1) {
				log.error("Error reading keyfile", e1);
				setKey(null);
			}
			catch (ConfigurationException e1) {
				log.error("Error reading keyfile", e1);
				setKey(null);
			}
		}
		else
			setKey(null);
	}

	public File getKeyFolder() {
		return keyFolder;
	}

	public void setKeyFolder(File keyFolder) {
		Object old = this.keyFolder;
		this.keyFolder = keyFolder;
		firePropertyChange("keyFolder", old, keyFolder);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		Object old = this.password;
		this.password = password;
		firePropertyChange("password", old, password);
	}

	public String getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(String confirmation) {
		Object old = this.confirmation;
		this.confirmation = confirmation;
		firePropertyChange("confirmation", old, confirmation);
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		Object old = this.key;
		this.key = key;
		firePropertyChange("key", old, key);
	}

	public String getMessageKey() {
		return "PasswordDialog";
	}

	// Generation 'PasswordDialog' START
	public com.jeta.forms.components.label.JETALabel getLblPassword() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblPassword");
	}

	public com.jeta.forms.components.label.JETALabel getLblConfirmation() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblConfirmation");
	}

	public javax.swing.JButton getBtnKeyFile() {
		return (javax.swing.JButton) getView().getProperty("btnKeyFile");
	}

	public javax.swing.JButton getBtnCancel() {
		return (javax.swing.JButton) getView().getProperty("btnCancel");
	}

	public javax.swing.JButton getBtnOk() {
		return (javax.swing.JButton) getView().getProperty("btnOk");
	}

	public javax.swing.JPasswordField getTfPassword() {
		return (javax.swing.JPasswordField) getView().getProperty("tfPassword");
	}

	public javax.swing.JPasswordField getTfConfirmation() {
		return (javax.swing.JPasswordField) getView().getProperty("tfConfirmation");
	}

	public com.jeta.forms.components.label.JETALabel getLblDescription() {
		return (com.jeta.forms.components.label.JETALabel) getView().getProperty("lblDescription");
	}
	// Generation 'PasswordDialog' END
}

package org.aklein.swap.examples.security;

import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.aklein.swap.examples.security.resources.SecurityUserManager;
import org.aklein.swap.security.UserManager;
import org.aklein.swap.security.XXmlConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DefaultUserManager implements UserManager<DefaultUserManager.User>, SecurityUserManager {
	private static Log			log	= LogFactory.getLog(DefaultUserManager.class);
	private File				userFolder;
	private File				keyFolder;
	private Window				parent;
	private Map<String, User>	map	= new HashMap<String, User>();
	@Autowired
	@Qualifier("UserDialog")
	private UserDialog			dialog;

	public DefaultUserManager() {
		super();
	}

	public boolean editUser(String name, InputStream file, Key masterKey) {
		User user = getUser(name, file, masterKey);
		if (user == null)
			return false;

		if (openEditorDialog(user, file, masterKey))
			return true;
		return false;
	}

	protected boolean openEditorDialog(User user, InputStream file, Key masterKey) {
		dialog.open(parent, keyFolder, user);
		if (dialog.isOk())
			user.copy(dialog.getUser());
		return dialog.isOk();
	}

	protected String createUserInfos(User user, Key masterKey) {
		String info = "";
		if (user != null && user.getConfig() != null)
			info = user.getConfig().getString("description", "");
		return info;
	}

	public Key getMasterKey(String name, InputStream file) {
		User user = getUser(name, file, null);
		if (user == null)
			return null;
		else
			return user.getMasterKey();
	}

	public String getUserInfos(String name, InputStream file, Key masterKey) {
		User user = null;
		try {
			if (file == null)
				return "";
			user = map.get(name);
			if (user == null) {
				if (masterKey == null)
					return "";
				user = loadUser(name, file, masterKey, null);
				map.put(name, user);
			}
		}
		catch (ConfigurationException e) {
			log.error("Error loading User", e);
			return "";
		}
		catch (IOException e) {
			log.error("Error loading User", e);
			return "";
		}
		catch (GeneralSecurityException e) {
			log.error("Error loading User", e);
			return "";
		}
		return createUserInfos(user, masterKey);
	}

	public String getUserInfos(User user, Key masterKey) {
		return createUserInfos(user, masterKey);
	}

	public Boolean isValid(String name, InputStream file, Key masterKey, boolean force) {
		User user;
		if (force) {
			user = getUser(name, file, masterKey);
		}
		else
			user = map.get(name);
		if (user == null)
			return null;
		return user.getMasterKey().equals(masterKey);
	}

	public boolean removeUser(String name, Key masterKey) {
		File file = new File(getUserFolder(), name);
		if (file.exists() && file.delete()) {
			map.remove(name);
			return true;
		}
		return false;
	}

	public User createUser(Key masterKey) {
		User user;

		user = new User();
		user.setMasterKey(masterKey);

		ResourceMap map = Application.getInstance().getContext().getResourceMap(DefaultUserManager.class);
		String username = JOptionPane.showInputDialog(this.parent, map.getString("username.message"), map.getString("username.title"), JOptionPane.QUESTION_MESSAGE);
		if (username == null)
			return null;
		username.replaceAll("/", "_");
		username.replaceAll("\\*", "_");
		username.replaceAll("\\\\", "_");
		username.replaceAll("\\?", "_");
		username.replaceAll("\\-", "_");
		username.replaceAll(">", "_");
		username.replaceAll("<", "_");
		username.replaceAll("\\|", "_");
		// File file = new File(getUserFolder(), username);
		user.getConfig().setProperty("name", username);

		if (openEditorDialog(user, null, masterKey))
			return user;

		return null;
	}

	private User loadUser(String name, InputStream file, Key masterKey, Key secretKey) throws IOException, GeneralSecurityException, ConfigurationException {
		PushbackInputStream is = null;
		User user = new User();
		try {
			is = new PushbackInputStream(file, 1024);
			ByteArrayOutputStream mKey = new ByteArrayOutputStream();
			ByteArrayOutputStream config = new ByteArrayOutputStream();
			StoppingInputStream fis = new StoppingInputStream(is, '~');
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) > 0)
				mKey.write(buffer, 0, len);

			user.setEncryptedMasterKey(mKey.toByteArray());
			mKey = null;
			if (masterKey == null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				if (secretKey == null)
					secretKey = PasswordDialog.open(parent, Application.getInstance().getContext().getResourceMap(DefaultUserManager.class).getString("password.message"), keyFolder, true);
				if (secretKey == null)
					return null;
				XXmlConfiguration.getCryptTool().decrypt(new BufferedInputStream(new ByteArrayInputStream(user.getEncryptedMasterKey())), bos, secretKey);
				masterKey = XXmlConfiguration.getCryptTool().parseKey(new String(bos.toByteArray()));
			}
			user.setMasterKey(masterKey);
			user.setSecureKey(secretKey);
			buffer = new byte[1024];
			while ((len = fis.read(buffer)) > 0)
				config.write(buffer, 0, len);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XXmlConfiguration.getCryptTool().decrypt(new BufferedInputStream(new ByteArrayInputStream(config.toByteArray())), bos, masterKey);
			config = null;
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			bos = null;
			user.getConfig().load(bis);
			bis = null;
		}
		finally {
			is.close();
		}
		return user;
	}

	public User authenticateUser(String name, InputStream file, Key key) {
		try {
			return loadUser(name, file, null, key);
		}
		catch (ConfigurationException e) {
			log.error("Error authenticating user", e);
		}
		catch (IOException e) {
			log.error("Error authenticating user", e);
		}
		catch (GeneralSecurityException e) {
			log.error("Error authenticating user", e);
		}
		return null;
	}

	public boolean saveUser(User user, OutputStream file, Key masterKey) {
		try {
			if (masterKey == null)
				masterKey = user.getMasterKey();
			try {
				user.updateEncryptedMasterKey();
				file.write(user.getEncryptedMasterKey());
				file.write('~');
				StringWriter buffer = new StringWriter();
				user.getConfig().save(buffer);
				ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toString().getBytes());
				buffer = null;
				XXmlConfiguration.getCryptTool().encrypt(bis, masterKey, file, true);
				return true;
			}
			catch (ConfigurationException e) {
				log.error("Error saving user", e);
				return false;
			}
			catch (IOException e) {
				log.error("Error saving user", e);
				return false;
			}
		}
		finally {
			try {
				file.flush();
			}
			catch (IOException e) {}
		}
	}

	public User getUser(String name, InputStream file, Key masterKey) {
		if (file == null)
			return null;
		try {
			User user = map.get(name);
			if (user == null) {
				user = loadUser(name, file, masterKey, null);
				map.put(name, user);
			}
			return user;
		}
		catch (ConfigurationException e) {
			log.error("Error loading User", e);
			return null;
		}
		catch (IOException e) {
			log.error("Error loading User", e);
			return null;
		}
		catch (GeneralSecurityException e) {
			log.error("Error loading User", e);
			return null;
		}
	}

	public File getUserFolder() {
		return userFolder;
	}

	public void setUserFolder(File userFolder) {
		this.userFolder = userFolder;
	}

	public File getKeyFolder() {
		return keyFolder;
	}

	public void setKeyFolder(File keyFolder) {
		this.keyFolder = keyFolder;
		this.dialog.setKeyFolder(keyFolder);
	}

	public Window getParent() {
		return parent;
	}

	public void setParent(Window parent) {
		this.parent = parent;
		this.dialog.setParent(parent);
	}

	public static class User implements org.aklein.swap.security.User {
		Key					masterKey;
		Key					secureKey;
		byte[]				encryptedMasterKey;

		XMLConfiguration	config	= new XMLConfiguration();

		public Key getMasterKey() {
			return masterKey;
		}

		public void setMasterKey(Key masterKey) {
			this.masterKey = masterKey;
		}

		public XMLConfiguration getConfig() {
			return config;
		}

		public byte[] getEncryptedMasterKey() {
			return encryptedMasterKey;
		}

		public void setEncryptedMasterKey(byte[] encryptedMasterKey) {
			this.encryptedMasterKey = encryptedMasterKey;
		}

		public Key getSecureKey() {
			return secureKey;
		}

		public void setSecureKey(Key secureKey) {
			this.secureKey = secureKey;
		}

		public void updateEncryptedMasterKey() throws ConfigurationException {
			if (getSecureKey() != null) {
				String mKey = XXmlConfiguration.getCryptTool().encode(masterKey);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				XXmlConfiguration.getCryptTool().encrypt(new ByteArrayInputStream(mKey.getBytes()), getSecureKey(), bos, true);
				setEncryptedMasterKey(bos.toByteArray());
			}
		}

		@Override
		public Object clone() {
			User u = new User();
			u.setSecureKey(getSecureKey());
			u.setEncryptedMasterKey(getEncryptedMasterKey());
			u.setMasterKey(getMasterKey());
			u.config = (XMLConfiguration) u.config.clone();
			return u;
		}

		public void copy(User user) {
			setSecureKey(user.getSecureKey());
			setEncryptedMasterKey(user.getEncryptedMasterKey());
			setMasterKey(user.getMasterKey());
			config.clear();
			config.copy(user.getConfig());
		}

		public String getName() {
			return getConfig().getString("name");
		}

	}
}

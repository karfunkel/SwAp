package org.aklein.swap.security;

import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aklein.swap.ui.ViewController;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BasicKeyWallet implements KeyWallet {
	private XXmlConfiguration	wallet		= new XXmlConfiguration();
	private Map<String, String>	allValues	= new HashMap<String, String>();
	private static Log			log			= LogFactory.getLog(BasicKeyWallet.class);
	private boolean				loaded;
	private Key					walletKey	= null;

	public BasicKeyWallet() {
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void loadWallet(InputStream data) {
		if (data == null)
			return;
		try {
			allValues.clear();
			loaded = false;
			boolean containsKey = false;
			Key key = walletKey;
			if (key == null || XXmlConfiguration.getCryptTool().containsKey(key))
				containsKey = true;
			else
				XXmlConfiguration.getCryptTool().addKey(key);
			this.wallet.load(data);
			if (!containsKey)
				XXmlConfiguration.getCryptTool().removeKey(key);
			for (Iterator iterator = this.wallet.getKeys(); iterator.hasNext();) {
				String cfgKey = (String) iterator.next();
				if (cfgKey.length() > 0)
					allValues.put(cfgKey, this.wallet.getString(cfgKey));
			}
			loaded = true;
		}
		catch (ConfigurationException e) {
			log.error("Error loading wallet", e);
		}
	}

	public void addToWallet(String key, String data) {
		wallet.setProperty(key, data);
		allValues.put(key, data);
	}

	public void setWalletValue(String key, String data) {
		addToWallet(key, data);
	}

	public boolean exists(String data) {
		return allValues.containsValue(data);
	}

	public String keyInWallet(String data) {
		for (Map.Entry<String, String> entry : allValues.entrySet()) {
			if (entry.getValue().equals(data))
				return entry.getKey();
		}
		return null;
	}

	public void removeFromWallet(String key) {
		String old = this.wallet.getString(key);
		this.wallet.setProperty(key, null);
		if (old != null)
			allValues.remove(old);
	}

	public Map<String, String> getEntries() {
		return new HashMap<String, String>(allValues);
	}

	public void saveWallet(OutputStream target) {
		if (target == null)
			return;
		try {
			boolean containsKey = false;
			Key key = walletKey;
			if (key == null || XXmlConfiguration.getCryptTool().containsKey(key))
				containsKey = true;
			else
				XXmlConfiguration.getCryptTool().addKey(key);
			this.wallet.save(target, key);
			if (!containsKey)
				XXmlConfiguration.getCryptTool().removeKey(key);
		}
		catch (ConfigurationException e) {
			log.error("Error saving wallet", e);
		}
	}

	public Key parseKey(String key) throws GeneralSecurityException, ConfigurationException, IOException {
		if (XXmlConfiguration.getCryptTool().isEncrypted(key)) {
			throw new UnsupportedOperationException("This KeyWallet implementation does not support encrypted keys");
		}
		return XXmlConfiguration.getCryptTool().parseKey(key);
	}

	public boolean isEncrypted() {
		return walletKey != null;
	}

	public void setEncrypted(Key key) {
		walletKey = key;
	}

	public ViewController<? extends Container> getParent() {
		return null;
	}
}
